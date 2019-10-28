package com.medos.mos.ui.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.model.MedicineAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.medicalAppointment.medicalAppointmentFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MedicineApptBookingAdapter extends RecyclerView.Adapter<MedicineApptBookingAdapter.AppointmentViewHolder>{
    private static final String TAG = "MedicalBookAdapter";
    List<MedicineAppointment> mAppt;
    Context context;
    private final LayoutInflater mInflater;
    Utils util;
    SharedPreferences pref;

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
                    Log.d(TAG, "SummaryID : " +appt.getSummaryID());

                    //open dialog to confirm
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Confirm PickUp Booking");
                    alertDialog.setMessage("PickUp Details :" + "\n" +
                            "Date: " + date  + "\n" +
                            "Time: " + time + "\n");
                    alertDialog.setPositiveButton("Book", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //generate token first
                            String token = util.generateToken(context.getResources().getString(R.string.SPIK), context.getResources().getString(R.string.issuer), pref.getString("sessionToken", ""));
                            JSONObject appt_submit = new JSONObject();

                            //POST Method to be implemented
                            try {
                                appt_submit.put("medicalAppointmentDate", date);
                                appt_submit.put("medicalAppointmentBookingHours", timeID);
                                appt_submit.put("medicalAppointmentNotes", "Consultation");

                                HttpCall httpCallPost = new HttpCall();
                                httpCallPost.setHeader(token);
                                httpCallPost.setMethodtype(HttpCall.POST);
                                httpCallPost.setUrl(util.MEDICINEAPPTBOOK + appt.getSummaryID());
                                httpCallPost.setParams(appt_submit);

                                Log.d(TAG, "Post Request " + util.MEDICINEAPPTBOOK + appt.getSummaryID());

                                final Activity activity = (Activity) context;
//                                new HttpRequests(activity) {
//                                    @Override
//                                    public void onResponse(String response) {
//                                        super.onResponse(response);
//                                        Log.d(TAG, "JWT response: " + response);
//                                        try {
//                                            final DecodedJWT decodedJWT = JWT.decode(response);
//                                            if(JWTUtils.verifySignature(activity.getResources().getString(R.string.SPK), decodedJWT)) {
//                                                String[] tokenResponse = JWTUtils.decoded(response);
//                                                JSONObject obj = new JSONObject(tokenResponse[1]);
//
//                                                String result = obj.getString("respond");
//                                                Log.d(TAG, result);
//
//                                                JSONObject respond = new JSONObject(result);
//
//                                                if (respond.getString("Success").equals("true")) {
//                                                    //store in sharedpreference
////                                                Fragment frag = new medicalAppointmentFragment();
////                                                AppCompatActivity a = (AppCompatActivity) context;
////                                                a.getSupportFragmentManager().popBackStack();
////                                                activity.finish();
//
//                                                }
//                                                else{
//                                                    Toast.makeText(activity, "Booking Failed", Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                            else{
//                                                Toast.makeText(activity, "Invalid Signature", Toast.LENGTH_SHORT).show();
//                                            }
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//
//
//                                    }
//                                }.execute(httpCallPost);


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
