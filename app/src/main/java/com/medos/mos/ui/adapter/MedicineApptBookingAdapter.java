package com.medos.mos.ui.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.medos.mos.AES;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.MainActivity;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicineAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.login.LoginActivity;
import com.medos.mos.ui.login.OTPActivity;
import com.medos.mos.ui.medicineAppointment.medicineAppointmentFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.medos.mos.ui.login.OTPActivity.decryptString;

public class MedicineApptBookingAdapter extends RecyclerView.Adapter<MedicineApptBookingAdapter.AppointmentViewHolder>{
    private static final String TAG = "MedicalBookAdapter";
    List<MedicineAppointment> mAppt;
    Context context;
    private final LayoutInflater mInflater;
    Utils util;
    SharedPreferences pref;
    OTPActivity otp;

    public MedicineApptBookingAdapter(List<MedicineAppointment> mAppt, Context context) {
        this.mAppt = mAppt;
        this.context = context;
        mInflater = LayoutInflater.from(context);
        util = new Utils();
        pref = context.getSharedPreferences("Session", 0); // 0 - for private mode
    }

    @NonNull
    @Override
    public MedicineApptBookingAdapter.AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.appointment_item, parent, false);
        return new MedicineApptBookingAdapter.AppointmentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineApptBookingAdapter.AppointmentViewHolder holder, int position) {
        final MedicineAppointment appt = mAppt.get(position);
        if(mAppt != null){
            //implement Post Method
        /*
        String summary_id = appt.getSummaryID();
         */

            holder.tvDate.setText(appt.getMedicineAppointmentDate());
            holder.tvHours.setText(appt.getMedicinrAppointmentBookingHours());
            holder.cardViewAppt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //get date and time
                    final String date = appt.getMedicineAppointmentDate();
                    final String time = appt.getMedicinrAppointmentBookingHours();
                    final int timeID = appt.getMedicineBookHourID();
                   // Log.d(TAG, "SummaryID : " +appt.getSummaryID());

                    //open dialog to confirm
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Confirm PickUp Booking");
                    alertDialog.setMessage("PickUp Details :" + "\n" +
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

                            //POST Method to be implemented
                            try {
                                appt_submit.put("medicineAppointmentDate", date);
                                appt_submit.put("medicineAppointmentBookingHours", timeID);
                                appt_submit.put("medicineAppointmentNotes", "Pick Up Medication");

                                HttpCall httpCallPost = new HttpCall();
                                httpCallPost.setHeader(token);
                                httpCallPost.setMethodtype(HttpCall.POST);
                                httpCallPost.setUrl(util.MEDICINEAPPTBOOK + appt.getMedicalID());
                                httpCallPost.setParams(appt_submit);

                                Log.d(TAG, "Post Request " + util.MEDICINEAPPTBOOK + appt.getMedicalID());

                                final Activity activity = (Activity) context;
                                new HttpRequests(activity) {
                                    @Override
                                    public void onResponse(String response) {
                                        super.onResponse(response);
                                        Log.d(TAG, "JWT response: " + response);
                                        try {
                                            final DecodedJWT decodedJWT = JWT.decode(response);
                                            if(JWTUtils.verifySignature(activity.getResources().getString(R.string.SPK), decodedJWT)) {
                                                String[] tokenResponse = JWTUtils.decoded(response);
                                                JSONObject obj = new JSONObject(tokenResponse[1]);

                                                String result = obj.getString("respond");
                                                Log.d(TAG, result);

                                                JSONObject respond = new JSONObject(result);

                                                if (respond.getString("Success").equals("true")) {
                                                    //store in sharedpreference
                                                Fragment frag = new medicineAppointmentFragment();
                                                AppCompatActivity a = (AppCompatActivity) context;
                                                a.getSupportFragmentManager().popBackStack();
                                                activity.finish();
                                                }
                                                else{
                                                    Toast.makeText(context, "Session Timeout", Toast.LENGTH_SHORT).show();
                                                    if(respond.getString("Error").equals("Invalid Token")){
                                                        //log user out
                                                        SharedPreferences.Editor editor;
                                                        editor = pref.edit();
                                                        editor.putString("sessionToken", null);
                                                        editor.putString("Phone", null);
                                                        editor.putString("Password", null);
                                                        editor.putString("LoginTimeStamp", null);
                                                        editor.commit();

                                                        Intent loginIntent = new Intent(context, LoginActivity.class);
                                                        context.startActivity(loginIntent);
                                                    }
                                                    activity.finish();
                                                }
                                            }
                                            else{
                                                Toast.makeText(activity, "Invalid Signature", Toast.LENGTH_SHORT).show();
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
