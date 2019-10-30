package com.medos.mos.ui.profile;


import android.app.Activity;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.medos.mos.ui.login.OTPActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class profileFragment extends Fragment {

    Utils util;
    SharedPreferences pref;
    private String TAG = "profileFragment";
    TextView tvName, tvage, tvgender, tvbo, tvAllergies, tvdob, tvAddr;
    OTPActivity otp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        util = new Utils();
        pref = getContext().getSharedPreferences("Session", 0); // 0 - for private mode
        tvName = root.findViewById(R.id.tvProfileName);
        tvage = root.findViewById(R.id.tvProfileAge);
        tvgender = root.findViewById(R.id.trvProfileGender);
        tvbo = root.findViewById(R.id.tvProfileBT);
        tvAllergies = root.findViewById(R.id.tvProfileAllergies);
        tvdob = root.findViewById(R.id.tvProfileDOB);
        tvAddr = root.findViewById(R.id.tvProfileAddress);
        retrieveProfile();

        return root;
    }

    private void retrieveProfile() {
        String token = util.generateToken(getResources().getString(R.string.SPIK), getResources().getString(R.string.issuer), otp.decryptString(this.getContext(), pref.getString("sessionToken", "")));
        HttpCall httpCallPost = new HttpCall();
        httpCallPost.setHeader(token);
        httpCallPost.setMethodtype(HttpCall.GET);
        httpCallPost.setUrl(util.PROFILEURL);

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
                            JSONObject profileObj = new JSONObject(respond.getString("Respond"));
                            tvName.setText(profileObj.getString("FullName"));
                            tvage.setText(profileObj.getString("age"));
                            tvgender.setText(profileObj.getString("Gender"));
                            tvbo.setText(profileObj.getString("BloodType"));
                            tvAllergies.setText(profileObj.getString("Allergies"));
                            tvdob.setText(profileObj.getString("DOB"));
                            tvAddr.setText(profileObj.getString("Address"));
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

}
