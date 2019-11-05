package com.medos.mos.ui.medicalAppointment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.medos.mos.AES;
import com.medos.mos.AppointmentDateActivity;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.MainActivity;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.login.OTPActivity;

import org.json.JSONException;
import org.json.JSONObject;

import static com.medos.mos.ui.login.OTPActivity.decryptString;

public class MedicalAppointmentDetail extends AppCompatActivity {
    private String TAG = "MedicalAppointmentDetail";
    SharedPreferences pref;
    Utils util;
    MedicalAppointment appt;
    OTPActivity otp;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_appointment_detail);
        appt = getIntent().getParcelableExtra("appointment");
        TextView tvDate, tvTime, tvStatus, tvNote;
        Button btnCancelAppt = findViewById(R.id.btnCancelAppt);
        Button btnBookPickUp = findViewById(R.id.btnBookMedicinePickup);
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

        if (!appt.getStatus().equals("Pending")) {
            btnCancelAppt.setEnabled(false);
            btnCancelAppt.setVisibility(View.GONE);
        }
        if (appt.getStatus().equals("Collection of Medicine")) {
            btnBookPickUp.setVisibility(View.VISIBLE);
            btnCancelAppt.setEnabled(false);
            btnCancelAppt.setClickable(false);
            btnCancelAppt.setVisibility(View.GONE);
        }
    }


    public void bookPickUp(View view){
        Intent intent = new Intent(MedicalAppointmentDetail.this, AppointmentDateActivity.class);
        intent.putExtra("appt",appt);
        startActivity(intent);
        finish();
    }


    public void cancelAppt(View view) {
        context = this;
        //open dialog to confirm
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Cancel Appointment Booking");
        alertDialog.setMessage("Do you wish to cancel?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //TAO
                Log.d(TAG, "Finding Spik");
                String enRsaKey = decryptString(context, pref.getString("rsk", ""));
                String rsaKey = AES.getRsaKey(enRsaKey);
                String SPIK = AES.decryptRsa(rsaKey);


                //generate token first
                String token = util.generateToken(SPIK, getResources().getString(R.string.issuer), otp.decryptString(context, pref.getString("sessionToken", "")));
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
                                final DecodedJWT decodedJWT = JWT.decode(response);
                                if(JWTUtils.verifySignature(getResources().getString(R.string.SPK), decodedJWT)) {
                                    JSONObject obj = new JSONObject(tokenResponse[1]);

                                    String result = obj.getString("respond");
                                    Log.d(TAG, result);

                                    JSONObject respond = new JSONObject(result);

                                    if (respond.getString("Success").equals("true")) {
                                        //store in sharedpreference
                                        finish();
                                    } else{
                                        Toast.makeText(getApplicationContext(), "Session Timeout", Toast.LENGTH_SHORT).show();
                                        if(respond.getString("Error").equals("Invalid Token")){
                                            //log user out
                                            MainActivity a = new MainActivity();
                                            a.logoutUser();
                                        }
                                    }
                                }
                                else{
                                    Toast.makeText(MedicalAppointmentDetail.this, "Invalid Signature", Toast.LENGTH_SHORT).show();
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
