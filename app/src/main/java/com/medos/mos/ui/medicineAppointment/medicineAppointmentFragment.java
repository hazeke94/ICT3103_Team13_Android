package com.medos.mos.ui.medicineAppointment;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.adapter.MedicalApptAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class medicineAppointmentFragment extends Fragment {

    RecyclerView rvMedicineAppt;
    ArrayList<MedicalAppointment> mAppt = new ArrayList<>();
    Utils util;
    SharedPreferences pref;
    String TAG = "medicineApptFrag";
    MedicalApptAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_medicine_appointment, container, false);
        util = new Utils();
        pref = getActivity().getSharedPreferences("Session", 0); // 0 - for private mode

        //call method to get medical appointment
        //retrieveAppointmentDate();
        return root;
    }

    public void retrieveAppointmentDate(){
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

                                MedicalAppointment appt = new MedicalAppointment(json.getString("MedicalAppointmentDate"), json.getString("MedicalAppointmentNotes"), json.getString("MedicalAppointmentBookingHours"), 0);
                                appt.setStatus(json.getString("MedicalAppointmentStatus"));
                                mAppt.add(appt);
                                Log.d(TAG, json.getString("MedicalAppointmentDate"));
                                Log.d(TAG, json.getString("MedicalAppointmentBookingHours"));
                                Log.d(TAG, json.getString("MedicalAppointmentNotes"));
                            }
                        }
                        if (mAppt.size() != 0) {
                            //throw into adapter to show list of appt
                            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                            rvMedicineAppt.setLayoutManager(layoutManager);
                            adapter = new MedicalApptAdapter(mAppt, getActivity());
                            rvMedicineAppt.setAdapter(adapter);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.execute(httpCallPost);

    }

}
