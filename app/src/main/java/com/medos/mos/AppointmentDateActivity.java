package com.medos.mos;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.model.MedicineAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.adapter.MedicineApptBookingAdapter;
import com.medos.mos.ui.login.LoginActivity;
import com.medos.mos.ui.login.OTPActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static com.medos.mos.ui.login.OTPActivity.decryptString;

public class AppointmentDateActivity extends AppCompatActivity {

    Button btnDatePicker;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay;

    RecyclerView rvTimeSlot;
    ArrayList<MedicineAppointment> medicineApptList = new ArrayList<>();

    String TAG = "AppointmentDateFragment";
    Utils util;
    SharedPreferences pref;
    MedicalAppointment med_appt;
    OTPActivity otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_appointment_date);

        med_appt = getIntent().getParcelableExtra("appt");
        btnDatePicker= findViewById(R.id.btn_date);
        txtDate=findViewById(R.id.in_date);
        rvTimeSlot = findViewById(R.id.rvAppointmentSlots);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvTimeSlot);
        util = new Utils();

        pref = getSharedPreferences("Session", 0); // 0 - for private mode

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
                Log.d(TAG, "Date is : " + txtDate.getText());
            }
        });
    }

    public void selectDate(){
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        txtDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);

                        //call get function
                        retrieveAppointmentDate((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void retrieveAppointmentDate(final String date){
        try {

            //TAO
            Log.d(TAG, "Finding Spik");
            String enRsaKey = decryptString(this, pref.getString("rsk", ""));
            String rsaKey = AES.getRsaKey(enRsaKey);
            String SPIK = AES.decryptRsa(rsaKey);

            //We will sign our JWT with our ApiKey secret
            String token = util.generateToken(SPIK, getResources().getString(R.string.issuer), otp.decryptString(this, pref.getString("sessionToken", "")));
            Log.d(TAG,token);

            final JSONObject appointment = new JSONObject();
            appointment.put("StartDate", date);
            //get request for appointment
            HttpCall httpCallPost = new HttpCall();
            httpCallPost.setHeader(token);
            httpCallPost.setMethodtype(HttpCall.GET);
            httpCallPost.setUrl(util.AvailableMedicineSlotsGetURL);

            httpCallPost.setParams(appointment);

            new HttpRequests(this) {
                @Override
                public void onResponse(String response) {
                    super.onResponse(response);
                    Log.d(TAG, "JWT response: " + response);
                    String[] tokenResponse = new String[2];
                    try {
                        if(!response.equals("")){
                            final DecodedJWT decodedJWT = JWT.decode(response);
                            if(JWTUtils.verifySignature(getResources().getString(R.string.SPK), decodedJWT)) {
                                tokenResponse = JWTUtils.decoded(response);
                                JSONObject obj = new JSONObject(tokenResponse[1]);
                                Log.d(TAG, obj.getString("respond"));
                                String result = obj.getString("respond");
                                JSONObject respond = new JSONObject(result);

                                if (respond.getString("Success").equals("true")) {
                                    //get list of dates
                                    medicineApptList = new ArrayList<>();
                                    JSONArray appointmentList = respond.getJSONArray("Respond");
                                    int length = appointmentList.length();
                                    for (int i = 0; i < length; i++) {
                                        JSONObject json = appointmentList.getJSONObject(i);

                                        //set SummaryID for mbooking of medicine appointment
                                        MedicineAppointment appt = new MedicineAppointment(date, med_appt.getMedicalAppointmentNotes(), json.getString("BookingHoursTime"), json.getInt("BookingHoursId"), 0, med_appt.getMedicalID(), "");
                                        medicineApptList.add(appt);
                                        Log.d(TAG, json.getString("BookingHoursId"));
                                        Log.d(TAG, json.getString("BookingHoursTime"));
                                    }
                                    if (medicineApptList != null) {
                                        //throw into adapter to show list of appt
                                        LinearLayoutManager layoutManager = new LinearLayoutManager((getApplicationContext()));
                                        rvTimeSlot.setHasFixedSize(true);
                                        rvTimeSlot.setLayoutManager(layoutManager);
                                        MedicineApptBookingAdapter adapter = new MedicineApptBookingAdapter(medicineApptList, AppointmentDateActivity.this);
                                        rvTimeSlot.setAdapter(adapter);
                                    }
                                }
                                else{
                                    if(respond.getString("Error").equals("Invalid Token")){
                                        //log user out
                                        //log user out
                                        SharedPreferences.Editor editor;
                                        editor = pref.edit();
                                        editor.putString("sessionToken", null);
                                        editor.putString("Phone", null);
                                        editor.putString("Password", null);
                                        editor.putString("LoginTimeStamp", null);
                                        editor.commit();

                                        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(loginIntent);
                                    }
                                }
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Invalid Signature", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }.execute(httpCallPost);

        }  catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
