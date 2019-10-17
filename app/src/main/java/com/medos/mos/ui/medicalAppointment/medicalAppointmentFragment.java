package com.medos.mos.ui.medicalAppointment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.appointmentDateFragment;
import com.medos.mos.model.MedicalAppointment;
import com.medos.mos.model.Payload;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.adapter.MedicalApptAdapter;
import com.medos.mos.ui.adapter.MedicalApptBookingAdapter;

import org.json.JSONArray;
import org.json.JSONException;
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





        util = new Utils();
        pref = getActivity().getSharedPreferences("Session", 0); // 0 - for private mode

        //call method to get medical appointment
        retrieveAppointmentDate();

        //request appointment
        fabAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new appointmentDateFragment();

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
        Payload payload;
        Map<String, Object> headerClaims = new HashMap();
        headerClaims.put("alg", "RS256");
        headerClaims.put("typ", "JWT");

        payload = util.generatePayload(getResources().getString(R.string.issuer));

        try {
            //We will sign our JWT with our ApiKey secret
            String privateKey = getResources().getString(R.string.SPIK);
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
                    .withClaim("token", pref.getString("sessionToken", ""))
                    .sign(algorithm);
            Log.d(TAG, token);


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
                                rvMedAppt.setLayoutManager(layoutManager);
                                adapter = new MedicalApptAdapter(mAppt, getActivity());
                                rvMedAppt.setAdapter(adapter);
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
        }
    }
}
