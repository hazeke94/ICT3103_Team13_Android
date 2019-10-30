package com.medos.mos.ui.medicalAppointment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.MainActivity;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.MedicalappointmentDateFragment;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.model.Payload;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.adapter.MedicalApptAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class medicalAppointmentFragment extends Fragment {

    FloatingActionButton fabAppointment;
    RecyclerView rvMedAppt;
    ArrayList<MedicalAppointment> mAppt = new ArrayList<>();
    Utils util;
    SharedPreferences pref;
    String TAG = "medicalApptFrag";
    MedicalApptAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_medical_appointment, container, false);
        fabAppointment = root.findViewById(R.id.btnAppointment);
        rvMedAppt = root.findViewById(R.id.recyclerViewMedicalAppointment);
        final SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.MedicalSwipeRefreshLayout);
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

        util = new Utils();
        pref = getActivity().getSharedPreferences("Session", 0); // 0 - for private mode

        //request appointment
        fabAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new MedicalappointmentDateFragment();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment, frag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack("medicalFragment");
                ft.commit();
            }
        });
        return root;
    }

    public void retrieveAppointmentDate() {
        try {
            //create token to be sent for otp
            String token = util.generateToken(getResources().getString(R.string.SPIK), getResources().getString(R.string.issuer), pref.getString("sessionToken", ""));            Log.d(TAG, token);

            //get request for appointment
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
                        if(JWTUtils.verifySignature(getResources().getString(R.string.SPK), decodedJWT)) {
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
                                if (length != 0) {
                                    for (int i = 0; i < length; i++) {
                                        JSONObject json = appointmentList.getJSONObject(i);

                                        MedicalAppointment appt = new MedicalAppointment(json.getString("MedicalAppointmentDate"), json.getString("MedicalAppointmentNotes"), json.getString("MedicalAppointmentBookingHours"), 0);
                                        appt.setStatus(json.getString("MedicalAppointmentStatus"));
                                        appt.setMedicalID(json.getInt("MedicalAppointmentId"));
                                        mAppt.add(appt);
                                        Log.d(TAG, json.getString("MedicalAppointmentDate"));
                                        Log.d(TAG, json.getString("MedicalAppointmentBookingHours"));
                                        Log.d(TAG, json.getString("MedicalAppointmentNotes"));
                                    }
                                }
                                if (mAppt.size() != 0) {
                                    //throw into adapter to show list of appt
                                    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                    rvMedAppt.setLayoutManager(layoutManager);
                                    adapter = new MedicalApptAdapter(mAppt, getActivity());
                                    rvMedAppt.setAdapter(adapter);
                                }
                            } else {
                                Toast.makeText(getContext(), "Session Timeout", Toast.LENGTH_SHORT).show();
                                if (respond.getString("Error").equals("Invalid Token")) {
                                    //log user out
                                    MainActivity a = new MainActivity();
                                    a.logoutUser();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute(httpCallPost);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveAppointmentDate();
    }

    @Override
    public void onResume() {
        super.onResume();
        Long timestamp = System.currentTimeMillis() / 1000;
        Long loginStamp = pref.getLong("LoginTimeStamp", 0);
        Long difference = timestamp - loginStamp;
        if(difference >= 3600){
            Toast.makeText(getContext(), "Session Timeout", Toast.LENGTH_SHORT).show();
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
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
