package com.medos.mos.ui.login;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.MainActivity;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.ui.JWTUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class OTPActivity extends AppCompatActivity {
    String phone;
    String password;
    Utils util;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static final String TAG = "OTPActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        phone = getIntent().getStringExtra("phone");
        password = getIntent().getStringExtra("password");

        util = new Utils();
        pref = getApplicationContext().getSharedPreferences("Session", 0); // 0 - for private mode
        editor = pref.edit();
    }

    public void sendOTP(View view) {
        EditText edOTP = findViewById(R.id.edOTP);
        int otp_input = Integer.parseInt(edOTP.getText().toString());

        try {
            String token = util.generateToken(getResources().getString(R.string.SPIK), getResources().getString(R.string.issuer));
            Log.d(TAG,token);
            JSONObject otp_submit = new JSONObject();
            otp_submit.put("otp", otp_input);
            otp_submit.put("phone", phone);

            HttpCall httpCallPost = new HttpCall();
            httpCallPost.setHeader(token);
            httpCallPost.setMethodtype(HttpCall.POST);
            httpCallPost.setUrl(util.OTPAPIURL);

            httpCallPost.setParams(otp_submit);
            new HttpRequests(this) {
                @Override
                public void onResponse(String response) {
                    super.onResponse(response);
                    Log.d(TAG, "JWT response: " + response);
                    try {
                        String[] tokenResponse = JWTUtils.decoded(response);
                        JSONObject obj = new JSONObject(tokenResponse[1]);
                        String result = obj.getString("respond");
                        JSONObject respond = new JSONObject(result);

                        if(respond.getString("Success").equals("true")){
                            //store in sharedpreference
                            JSONObject resObj = new JSONObject(respond.getString("Respond"));
                            long loginTimeStamp = System.currentTimeMillis() / 1000;
                            editor.putString("sessionToken", resObj.getString("sessiontoken"));
                            editor.putString("Phone", phone);
                            editor.putString("Password", password);
                            editor.putLong("LoginTimeStamp", loginTimeStamp);
                            editor.apply();
                            Log.d(TAG, resObj.getString("sessiontoken"));

                            Intent home = new Intent(getApplicationContext(), MainActivity.class);
                            home.putExtra("phone", phone);
                            startActivity(home);
                        }
                        else{
//                          //open dialog to confirm
                            Toast.makeText(OTPActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                        }



                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }.execute(httpCallPost);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }








}
