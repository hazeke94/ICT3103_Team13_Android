package com.medos.mos.ui.medicineAppointment;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.medos.mos.AES;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicineAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.adapter.MedicineApptAdapter;
import com.medos.mos.ui.login.LoginActivity;
import com.medos.mos.ui.login.OTPActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.medos.mos.ui.login.OTPActivity.decryptString;

/**
 * A simple {@link Fragment} subclass.
 */
public class medicineAppointmentFragment extends Fragment {

    RecyclerView rvMedicineAppt;
    ArrayList<MedicineAppointment> mAppt = new ArrayList<>();
    Utils util;
    SharedPreferences pref;
    String TAG = "medicineApptFrag";
    MedicineApptAdapter adapter;
    OTPActivity otp;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_medicine_appointment, container, false);
        util = new Utils();
        pref = getActivity().getSharedPreferences("Session", 0); // 0 - for private mode
        rvMedicineAppt = root.findViewById(R.id.recyclerViewMedicineAppointment);
        final SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.MedicineSwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        retrieveAppointmentDate();
                    }
                },1000);
            }
        });
        //call method to get medical appointment
        retrieveAppointmentDate();
        return root;
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void retrieveAppointmentDate(){

        //TAO
        Log.d(TAG, "Finding Spik");
        String enRsaKey = decryptString(this.getContext(), pref.getString("rsk", ""));
        String rsaKey = AES.getRsaKey(enRsaKey);
        String SPIK = AES.decryptRsa(rsaKey);

        String token = util.generateToken(SPIK, getResources().getString(R.string.issuer), otp.decryptString(this.getContext(), pref.getString("sessionToken", "")));
        HttpCall httpCallPost = new HttpCall();
        httpCallPost.setHeader(token);
        httpCallPost.setMethodtype(HttpCall.GET);
        httpCallPost.setUrl(util.MEDICINEAPPTURL);
        Activity activity = (Activity) getContext();
        new HttpRequests(activity) {
            @Override
            public void onResponse(String response) {
                super.onResponse(response);
                Log.d(TAG, "JWT response: " + response);
                String[] tokenResponse = new String[2];
                try {
                    tokenResponse = JWTUtils.decoded(response);
                    JSONObject obj = new JSONObject(tokenResponse[1]);
                    Log.d(TAG, obj.getString("respond"));
                    String result = obj.getString("respond");
                    JSONObject respond = new JSONObject(result);

                    if (respond.getString("Success").equals("true")) {
                        //get list of dates
                        mAppt = new ArrayList<>();
                        JSONArray appointmentList = respond.getJSONArray("Respond");
                        int length = appointmentList.length();
                        Log.d(TAG, String.valueOf(length));
                        if(length !=0) {
                            for (int i = 0; i < length; i++) {
                                JSONObject json = appointmentList.getJSONObject(i);

                                MedicineAppointment appt = new MedicineAppointment(json.getString("MedicineAppointmentDate"), json.getString("MedicineAppointmentNotes"), json.getString("MedicineAppointmentBookingHours"), 0,json.getInt("MedicineAppointmentId"),0,"");
                                mAppt.add(appt);
                                Log.d(TAG, json.getString("MedicineAppointmentDate"));
                                Log.d(TAG, json.getString("MedicineAppointmentNotes"));
                                Log.d(TAG, json.getString("MedicineAppointmentBookingHours"));
                            }
                        }
                        if (mAppt.size() != 0) {
                            //throw into adapter to show list of appt
                            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                            rvMedicineAppt.setLayoutManager(layoutManager);
                            adapter = new MedicineApptAdapter(mAppt, getActivity());
                            rvMedicineAppt.setAdapter(adapter);
                        }

                    }
                    else{
                        if (respond.getString("Error").equals("Invalid Token")) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.execute(httpCallPost);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();
        retrieveAppointmentDate();
        Long timestamp = System.currentTimeMillis() / 1000;

        String loginStamp_str = otp.decryptString(this.getContext(), pref.getString("LoginTimeStamp", null));
        Long loginStamp = Long.valueOf(loginStamp_str);

        Long difference = timestamp - loginStamp;
        if(difference >= 600){
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
                    editor.putString("LoginTimeStamp", null);
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
