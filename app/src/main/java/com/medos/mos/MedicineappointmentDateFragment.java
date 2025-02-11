package com.medos.mos;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.medos.mos.model.MedicineAppointment;
import com.medos.mos.model.Payload;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.adapter.MedicineApptBookingAdapter;
import com.medos.mos.ui.login.LoginActivity;
import com.medos.mos.ui.login.OTPActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.medos.mos.ui.login.OTPActivity.decryptString;


public class MedicineappointmentDateFragment extends Fragment {

    Button btnDatePicker, btnTimePicker;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;

    RecyclerView rvTimeSlot;
    ArrayList<MedicineAppointment> medicineApptList = new ArrayList<>();

    String TAG = "AppointmentDateFragment";
    Utils util;
    SharedPreferences pref;
    OTPActivity otp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_appointment_date, container, false);
        btnDatePicker= root.findViewById(R.id.btn_date);
        txtDate=root.findViewById(R.id.in_date);
        rvTimeSlot = root.findViewById(R.id.rvAppointmentSlots);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvTimeSlot);
        util = new Utils();

        pref = getActivity().getSharedPreferences("Session", 0); // 0 - for private mode

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
                Log.d(TAG, "Date is : " + txtDate.getText());
            }
        });

        return root;
    }

    public void selectDate(){
// Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        txtDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);

                        //call get function
                        //retrieveAppointmentDate((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void retrieveAppointmentDate(final String date){
        Payload payload;
        Map<String, Object> headerClaims = new HashMap();
        headerClaims.put("alg", "RS256");
        headerClaims.put("typ", "JWT");

        payload = util.generatePayload(getResources().getString(R.string.issuer));

        try {

            //TAO
            Log.d(TAG, "Finding Spik");
            String enRsaKey = decryptString(this.getContext(), pref.getString("rsk", ""));
            String rsaKey = AES.getRsaKey(enRsaKey);
            String SPIK = AES.decryptRsa(rsaKey);

            //We will sign our JWT with our ApiKey secret
            String privateKey = SPIK;
            privateKey = privateKey.replace("-----BEGIN RSA PRIVATE KEY-----", "");
            privateKey = privateKey.replace("-----END RSA PRIVATE KEY-----", "");
            privateKey = privateKey.replaceAll("\\s+", "");

            PrivateKey privKey = JWTUtils.generatePrivateKey(privateKey);
            Algorithm algorithm = Algorithm.RSA256(null, (RSAPrivateKey) privKey);

            //create token to be sent for otp
            String token = JWT.create()
                    .withHeader(headerClaims)
                    .withClaim("iss", payload.getIss())
                    .withClaim("exp", payload.getEx())
                    .withClaim("iat", payload.getIat())
                    .withClaim("token", otp.decryptString(this.getContext(), pref.getString("sessionToken","")))
                    .sign(algorithm);
            Log.d(TAG,token);

            final JSONObject appointment = new JSONObject();
            appointment.put("StartDate", date);
            //get request for appointment
            HttpCall httpCallPost = new HttpCall();
            httpCallPost.setHeader(token);
            httpCallPost.setMethodtype(HttpCall.GET);
            httpCallPost.setUrl(util.AvailableMedicalSlotsGETURL);

            httpCallPost.setParams(appointment);

            new HttpRequests(getActivity()) {
                @Override
                public void onResponse(String response) {
                    super.onResponse(response);
                    Log.d(TAG, "JWT response: " + response);
                    String[] tokenResponse = new String[2];
                    try {
                        if(!response.equals("")){
                            tokenResponse = JWTUtils.decoded(response);
                            JSONObject obj = new JSONObject(tokenResponse[1]);
                            Log.d(TAG, obj.getString("respond"));
                            String result = obj.getString("respond");
                            JSONObject respond = new JSONObject(result);

                            if(respond.getString("Success").equals("true")) {
                                //get list of dates
                                medicineApptList = new ArrayList<>();
                                JSONArray appointmentList = respond.getJSONArray("Respond");
                                int length = appointmentList.length();
                                for (int i = 0; i < length; i++) {
                                    JSONObject json = appointmentList.getJSONObject(i);

                                    //MedicineAppointment appt = new MedicineAppointment(medicineAppointmentDate,  medicineAppointmentNotes,  medicineAppointmentBookingHours,  medicineBookHourID,  medicineID,  summaryID,  status);
                                    MedicineAppointment appt = new MedicineAppointment("",  "",  "",  1,  1,  1,  "");
                                    medicineApptList.add(appt);
                                    Log.d(TAG, json.getString("BookingHoursId"));
                                    Log.d(TAG, json.getString("BookingHoursTime"));
                                }
                                if (medicineApptList != null) {
                                    //throw into adapter to show list of appt

                                    LinearLayoutManager layoutManager = new LinearLayoutManager((getActivity()));
                                    rvTimeSlot.setLayoutManager(layoutManager);
                                    MedicineApptBookingAdapter adapter = new MedicineApptBookingAdapter(medicineApptList, getActivity());
                                    rvTimeSlot.setAdapter(adapter);
                                }
                            }
                            else {
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

                                    Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                                    startActivity(loginIntent);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute(httpCallPost);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
