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
import com.medos.mos.model.MedicineAppointment;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.adapter.MedicalApptAdapter;
import com.medos.mos.ui.adapter.MedicineApptAdapter;

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
    ArrayList<MedicineAppointment> mAppt = new ArrayList<>();
    Utils util;
    SharedPreferences pref;
    String TAG = "medicineApptFrag";
    MedicineApptAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_medicine_appointment, container, false);
        util = new Utils();
        pref = getActivity().getSharedPreferences("Session", 0); // 0 - for private mode
        rvMedicineAppt = root.findViewById(R.id.recyclerViewMedicineAppointment);
        //call method to get medical appointment
        retrieveAppointmentDate();
        return root;
    }

    public void retrieveAppointmentDate(){
        String token = util.generateToken(getResources().getString(R.string.SPIK), getResources().getString(R.string.issuer), pref.getString("sessionToken", ""));
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.execute(httpCallPost);

    }

}
