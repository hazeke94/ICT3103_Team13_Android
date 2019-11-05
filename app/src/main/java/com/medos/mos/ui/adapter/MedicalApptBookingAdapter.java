package com.medos.mos.ui.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.medos.mos.AES;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.MainActivity;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.login.OTPActivity;
import com.medos.mos.ui.medicalAppointment.medicalAppointmentFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.medos.mos.ui.login.OTPActivity.decryptString;

public class MedicalApptBookingAdapter extends RecyclerView.Adapter<MedicalApptBookingAdapter.AppointmentViewHolder> {
    private static final String TAG = "MedicalBookAdapter";
    List<MedicalAppointment> mAppt;
    Context context;
    private final LayoutInflater mInflater;
    Utils util;
    SharedPreferences pref;
    OTPActivity otp;

    public MedicalApptBookingAdapter(List<MedicalAppointment> mAppt, Context context){
        this.mAppt = mAppt;
        this.context = context;
        mInflater = LayoutInflater.from(context);
        util = new Utils();
        pref = context.getSharedPreferences("Session", 0); // 0 - for private mode
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.appointment_item, parent, false);
        return new AppointmentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        if(mAppt!= null){
            final MedicalAppointment appt = mAppt.get(position);
            holder.tvDate.setText(appt.getMedicalAppointmentDate());
            holder.tvHours.setText(appt.getMedicalAppointmentBookingHours());
            holder.cardViewAppt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //get date and time
                    final String date = appt.getMedicalAppointmentDate();
                    final String time = appt.getMedicalAppointmentBookingHours();
                    final int timeID = appt.getMedicalBookHourID();

                    //open dialog to confirm
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Confirm Appointment Booking");
                    alertDialog.setMessage("Appointment Details :" + "\n" +
                            "Date: " + date  + "\n" +
                            "Time: " + time + "\n");
                    alertDialog.setPositiveButton("Book", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //TAO
                            Log.d(TAG, "Finding Spik");
                            String enRsaKey = decryptString(context, pref.getString("rsk", ""));
                            String rsaKey = AES.getRsaKey(enRsaKey);
                            String SPIK = AES.decryptRsa(rsaKey);

                            //generate token first
                            String token = util.generateToken(SPIK, context.getResources().getString(R.string.issuer), otp.decryptString(context, pref.getString("sessionToken", "")));
                            JSONObject appt_submit = new JSONObject();
                            try {
                                appt_submit.put("medicalAppointmentDate", date);
                                appt_submit.put("medicalAppointmentBookingHours", timeID);
                                appt_submit.put("medicalAppointmentNotes", "Consultation");

                                HttpCall httpCallPost = new HttpCall();
                                httpCallPost.setHeader(token);
                                httpCallPost.setMethodtype(HttpCall.POST);
                                httpCallPost.setUrl(util.MEDICALAPPTURL);

                                httpCallPost.setParams(appt_submit);
                                final Activity activity = (Activity) context;
                                new HttpRequests(activity) {
                                    @Override
                                    public void onResponse(String response) {
                                        super.onResponse(response);
                                        Log.d(TAG, "JWT response: " + response);
                                        try {
                                            String[] tokenResponse = JWTUtils.decoded(response);
                                            JSONObject obj = new JSONObject(tokenResponse[1]);

                                            String result = obj.getString("respond");
                                            Log.d(TAG, result);

                                            JSONObject respond = new JSONObject(result);

                                            if(respond.getString("Success").equals("true")){
                                                //store in sharedpreference
                                                Fragment frag = new medicalAppointmentFragment();
                                                AppCompatActivity a = (AppCompatActivity) context;
                                                a.getSupportFragmentManager().popBackStack();

                                            }
                                            else{
                                                Toast.makeText(context, "Session Timeout", Toast.LENGTH_SHORT).show();
                                                if(respond.getString("Error").equals("Invalid Token")){
                                                    //log user out
                                                    MainActivity a = new MainActivity();
                                                    a.logoutUser();
                                                }
                                            }



                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                    }
                                }.execute(httpCallPost);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // DO SOMETHING HERE
                            dialog.cancel();
                        }
                    });

                    AlertDialog dialog = alertDialog.create();
                    dialog.show();
                    //okay post

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mAppt.size();
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvHours;
        private final CardView cardViewAppt;
        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate =itemView.findViewById(R.id.tvApptDate);
            tvHours = itemView.findViewById(R.id.tvApptHour);
            cardViewAppt = itemView.findViewById(R.id.cardViewAppt);
        }
    }
}
