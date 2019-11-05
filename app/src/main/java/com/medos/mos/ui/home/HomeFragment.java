package com.medos.mos.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.MainActivity;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.adapter.MedicalApptAdapter;
import com.medos.mos.ui.login.LoginActivity;
import com.medos.mos.ui.medicineAppointment.medicineAppointmentFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;
    Utils util;
    SharedPreferences pref;
    ArrayList<MedicalAppointment> mAppt = new ArrayList<>();
    ArrayList<MedicalAppointment> mPickUp = new ArrayList<>();
    RecyclerView rvUpcoming, rvPickUp;
    MedicalApptAdapter adapter;
    MedicalApptAdapter pickUpAdapter;
    TextView tvMedical, tvMedicine;

    private String TAG = "homeFragment";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        util = new Utils();
        swipeRefreshLayout = root.findViewById(R.id.homeSwipeRefreshLayout);
        pref = getContext().getSharedPreferences("Session", 0); // 0 - for private mode
        rvUpcoming = root.findViewById(R.id.rvUpcoming_Appt);
        rvPickUp = root.findViewById(R.id.rvPickUpMedication);

        tvMedical = root.findViewById(R.id.tvMedicalAppt);
        tvMedicine = root.findViewById(R.id.tvMedicineAppt);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        //get current appointment
                        getCurrentAppointment();
                        getMedicationAppointment();
                    }
                },1000);
            }
        });
        return root;
    }

    private void getMedicationAppointment() {
        String token = util.generateToken(getResources().getString(R.string.SPIK), getResources().getString(R.string.issuer), pref.getString("sessionToken", ""));
        HttpCall httpCallPost = new HttpCall();
        httpCallPost.setHeader(token);
        httpCallPost.setMethodtype(HttpCall.GET);
        httpCallPost.setUrl(util.MEDICINEAPPTREQUEST);
        final Activity activity = (Activity) getContext();
        new HttpRequests(activity) {
            @Override
            public void onResponse(String response) {
                super.onResponse(response);
                Log.d(TAG, "JWT response: " + response);
                String[] tokenResponse = new String[2];
                try {
                    final DecodedJWT decodedJWT = JWT.decode(response);
                    if(JWTUtils.verifySignature(getResources().getString(R.string.SPK), decodedJWT)){
                        tokenResponse = JWTUtils.decoded(response);
                        JSONObject obj = new JSONObject(tokenResponse[1]);
                        Log.d(TAG, obj.getString("respond"));
                        String result = obj.getString("respond");
                        JSONObject respond = new JSONObject(result);

                        if (respond.getString("Success").equals("true")) {
                            //get list of dates
                            mAppt = new ArrayList<>();
                            mPickUp = new ArrayList<>();
                            JSONArray appointmentList = respond.getJSONArray("Respond");
                            int length = appointmentList.length();
                            Log.d(TAG, String.valueOf(length));
                            if(length !=0) {
                                Calendar calendar = null;
                                String date = "";
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    calendar = Calendar.getInstance();
                                    SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yyyy");
                                    date =mdformat.format(calendar.getTime());
                                }

                                for (int i = 0; i < length; i++) {
                                    JSONObject json = appointmentList.getJSONObject(i);

                                    MedicalAppointment appt = new MedicalAppointment(json.getString("MedicalAppointmentDate"), json.getString("MedicalAppointmentNotes"), json.getString("MedicalAppointmentBookingHours"), 0);
                                    appt.setStatus(json.getString("MedicalAppointmentStatus"));
                                    appt.setMedicalID(json.getInt("MedicalAppointmentId"));
                                    mPickUp.add(appt);
                                }
                            }
                                //throw into adapter to show list of appt
                            LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
                            rvPickUp.setLayoutManager(layoutManager1);
                            pickUpAdapter = new MedicalApptAdapter(mPickUp, getActivity());
                            if(mPickUp.size() == 0){
                                tvMedicine.setText("No Pick Up");
                            }

                            rvPickUp.setAdapter(pickUpAdapter);
                        }
                        else{
                            if(respond.getString("Error").equals("Invalid Token")){
                                Toast.makeText(getContext(), "Session Timeout", Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                                alertDialog.setTitle("Session Expired");
                                alertDialog.setMessage("Your Session has Expired.. Please Login again");
                                alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //log user out
                                        MainActivity a = new MainActivity();
                                        a.logoutUser();
                                    }
                                });
                                AlertDialog dialog = alertDialog.create();
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        }
                    }
                    else{
                        Toast.makeText(getContext(), "Invalid Signature", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.execute(httpCallPost);
    }

    public void getCurrentAppointment(){
        String token = util.generateToken(getResources().getString(R.string.SPIK), getResources().getString(R.string.issuer), pref.getString("sessionToken", ""));
        HttpCall httpCallPost = new HttpCall();
        httpCallPost.setHeader(token);
        httpCallPost.setMethodtype(HttpCall.GET);
        httpCallPost.setUrl(util.MEDICALAPPTURL);
        final Activity activity = (Activity) getContext();
        new HttpRequests(activity) {
            @Override
            public void onResponse(String response) {
                super.onResponse(response);
                Log.d(TAG, "JWT response: " + response);
                String[] tokenResponse = new String[2];
                try {
                    final DecodedJWT decodedJWT = JWT.decode(response);
                    if(JWTUtils.verifySignature(getResources().getString(R.string.SPK), decodedJWT)){
                        tokenResponse = JWTUtils.decoded(response);
                        JSONObject obj = new JSONObject(tokenResponse[1]);
                        Log.d(TAG, obj.getString("respond"));
                        String result = obj.getString("respond");
                        JSONObject respond = new JSONObject(result);

                        if (respond.getString("Success").equals("true")) {
                            //get list of dates
                            mAppt = new ArrayList<>();
                            mPickUp = new ArrayList<>();
                            JSONArray appointmentList = respond.getJSONArray("Respond");
                            int length = appointmentList.length();
                            Log.d(TAG, String.valueOf(length));
                            if(length !=0) {
                                Calendar calendar = null;
                                String date = "";
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    calendar = Calendar.getInstance();
                                    SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yyyy");
                                    date =mdformat.format(calendar.getTime());
                                }

                                for (int i = 0; i < length; i++) {
                                    JSONObject json = appointmentList.getJSONObject(i);

                                    MedicalAppointment appt = new MedicalAppointment(json.getString("MedicalAppointmentDate"), json.getString("MedicalAppointmentNotes"), json.getString("MedicalAppointmentBookingHours"), 0);
                                    appt.setStatus(json.getString("MedicalAppointmentStatus"));
                                    appt.setMedicalID(json.getInt("MedicalAppointmentId"));

                                    if(checkDate(appt.getMedicalAppointmentDate(), date)){
                                        if(appt.getStatus().equals("Pending") || appt.getStatus().equals("Confirmed")) {
                                            mAppt.add(appt);
                                            Log.d(TAG, json.getString("MedicalAppointmentDate"));
                                            Log.d(TAG, json.getString("MedicalAppointmentBookingHours"));
                                            Log.d(TAG, json.getString("MedicalAppointmentNotes"));
                                        }
                                    }
                                }
                            }
                                //throw into adapter to show list of appt
                                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                rvUpcoming.setLayoutManager(layoutManager);
                                if(mAppt.size() == 0){
                                    tvMedical.setText("No appointment");
                                }

                                adapter = new MedicalApptAdapter(mAppt, getActivity());
                                rvUpcoming.setAdapter(adapter);
                        }
                        else{
                            Toast.makeText(getContext(), "Session Timeout", Toast.LENGTH_SHORT).show();
                            if(respond.getString("Error").equals("Invalid Token")){
                                //log user out
                                MainActivity a = new MainActivity();
                                a.logoutUser();
                            }
                        }
                    }
                    else{
                        Toast.makeText(getContext(), "Invalid Signature", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.execute(httpCallPost);
    }

    private boolean checkDate(String date, String today) {
        SimpleDateFormat sdf = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date date1 = sdf.parse(date);
                Date date2 = sdf.parse(today);

                if(date1.after(date2)){
                    System.out.println("Date1 is after Date2");
                    return true;
                }

                //equals() returns true if both the dates are equal
                else if(date1.equals(date2)){
                    System.out.println("Date1 is equal Date2");
                    return true;
                }
                else{
                    return false;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        return false;

    }

    @Override
    public void onStart() {
        super.onStart();
        getCurrentAppointment();
        getMedicationAppointment();
    }

    @Override
    public void onResume() {
        super.onResume();
        Long timestamp = System.currentTimeMillis() / 1000;
        Long loginStamp = pref.getLong("LoginTimeStamp", 0);
        Long difference = timestamp - loginStamp;
        Log.d(TAG,"timestamp "  + timestamp);
        Log.d(TAG,"loginStamp "  + loginStamp);
        Log.d(TAG,"Difference "  + difference);
        if(difference >= 3600){
            Toast.makeText(getContext(), "Session Expired, Login Again!", Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
            alertDialog.setTitle("Session Expired");
            alertDialog.setMessage("Your Session has Expired.. Please Login again");
            alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //log user out
                    SharedPreferences.Editor editor;
                    editor = pref.edit();
                    editor.putString("sessionToken", null);
                    editor.putString("Phone", null);
                    editor.putString("Password", null);
                    editor.putLong("LoginTimeStamp", 0);
                    editor.commit();

                    Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                    startActivity(loginIntent);
                }
            });
            AlertDialog dialog = alertDialog.create();
            dialog.setCancelable(false);
            dialog.show();
        }
    }
}