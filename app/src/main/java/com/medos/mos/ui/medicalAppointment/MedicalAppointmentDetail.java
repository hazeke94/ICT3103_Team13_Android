package com.medos.mos.ui.medicalAppointment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.ui.JWTUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MedicalAppointmentDetail extends AppCompatActivity {
    private String TAG = "MedicalAppointmentDetail";
    SharedPreferences pref;
    Utils util;
    MedicalAppointment appt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_appointment_detail);
        appt = getIntent().getParcelableExtra("appointment");
        TextView tvDate,tvTime,tvStatus,tvNote;
        Button btnCancelAppt = findViewById(R.id.btnCancelAppt);

        pref = this.getSharedPreferences("Session", 0); // 0 - for private mode
        util = new Utils();

        tvDate = findViewById(R.id.tvDateDetail);
        tvTime = findViewById(R.id.tvTimeDetail);
        tvStatus = findViewById(R.id.tvStatusDetail);
        tvNote = findViewById(R.id.tvNoteDetail);

        tvDate.setText(appt.getMedicalAppointmentDate());
        tvNote.setText(appt.getMedicalAppointmentNotes());
        tvStatus.setText(appt.getStatus());
        tvTime.setText(appt.getMedicalAppointmentBookingHours());

        if(!appt.getStatus().equals("Pending")){
            btnCancelAppt.setEnabled(false);
            btnCancelAppt.setVisibility(View.GONE);
        }
    }

    public void cancelAppt(View view) {
            //open dialog to confirm
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Cancel Appointment Booking");
            alertDialog.setMessage("Do you wish to cancel?");
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //generate token first
                    String token = util.generateToken(getResources().getString(R.string.SPIK), getResources().getString(R.string.issuer), pref.getString("sessionToken", ""));
                    JSONObject cancel_appt = new JSONObject();

                    try {
                        cancel_appt.put("MedicalBookingID", appt.getMedicalID());


                        HttpCall httpCallPost = new HttpCall();
                        httpCallPost.setHeader(token);
                        httpCallPost.setMethodtype(HttpCall.GET);
                        httpCallPost.setUrl(util.CancelMedicalAppt);

                        httpCallPost.setParams(cancel_appt);

                        new HttpRequests(MedicalAppointmentDetail.this) {
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

                                    if (respond.getString("Success").equals("true")) {
                                        //store in sharedpreference
                                        finish();
                                    } else {
                                        Toast.makeText(MedicalAppointmentDetail.this, "Error : " + respond.getString("Errro"), Toast.LENGTH_SHORT).show();
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
    }
}
