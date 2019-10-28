package com.medos.mos.ui.home;

import android.app.Activity;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
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
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.adapter.MedicalApptAdapter;
import com.medos.mos.ui.login.LoginActivity;

import org.json.JSONArray;
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
    int year1 = 0;
    int month1 = 0;
    int day1 = 0;

    private String TAG = "homeFragment";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        util = new Utils();
        swipeRefreshLayout = root.findViewById(R.id.homeSwipeRefreshLayout);
        pref = getContext().getSharedPreferences("Session", 0); // 0 - for private mode
        rvUpcoming = root.findViewById(R.id.rvUpcoming_Appt);
        rvPickUp = root.findViewById(R.id.rvPickUpMedication);
        //get current appointment



        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        getCurrentAppointment();
                    }
                },1000);
            }
        });
        return root;
    }

    public void getCurrentAppointment(){
        Calendar c1 = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            c1 = Calendar.getInstance();
            c1.set(Calendar.DAY_OF_WEEK, 1);

            //first day of week
            year1 = c1.get(Calendar.YEAR);
            month1 = c1.get(Calendar.MONTH)+1;
            day1 = c1.get(Calendar.DAY_OF_MONTH);
        }


        String token = util.generateToken(getResources().getString(R.string.SPIK), getResources().getString(R.string.issuer), pref.getString("sessionToken", ""));
        HttpCall httpCallPost = new HttpCall();
        httpCallPost.setHeader(token);
        httpCallPost.setMethodtype(HttpCall.GET);
        httpCallPost.setUrl(util.MEDICALAPPTURL);
        Activity activity = (Activity) getContext();
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
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
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
                                    if(appt.getStatus().equals("Collection of Medicine")){
                                        mPickUp.add(appt);
                                        Log.d(TAG, json.getString("MedicalAppointmentDate"));
                                        Log.d(TAG, json.getString("MedicalAppointmentBookingHours"));
                                        Log.d(TAG, json.getString("MedicalAppointmentNotes"));
                                    }

                                }
                            }
                            if (mAppt.size() != 0 || mPickUp.size() != 0) {
                                //throw into adapter to show list of appt
                                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                rvUpcoming.setLayoutManager(layoutManager);
                                adapter = new MedicalApptAdapter(mAppt, getActivity());
                                rvUpcoming.setAdapter(adapter);

                            LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
                            rvPickUp.setLayoutManager(layoutManager1);
                            pickUpAdapter = new MedicalApptAdapter(mPickUp, getActivity());
                                rvPickUp.setAdapter(pickUpAdapter);
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
            //sdf = new SimpleDateFormat("yyyy-MM-dd");
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
    }
}