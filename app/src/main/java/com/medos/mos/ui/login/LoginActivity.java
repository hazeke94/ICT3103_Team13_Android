package com.medos.mos.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.ui.JWTUtils;

import org.json.JSONObject;
import java.util.HashMap;



public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    Utils util;
    HashMap<String, String> params = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        util = new Utils();
    }

    public void login(View view) {
        EditText edPhone = findViewById(R.id.editTextPhoneNumber);
        EditText edPassword = findViewById(R.id.editTextPassword);

//        final String phone = edPhone.getText().toString();
        final String phone = "81898811";
//        final String password = edPassword.getText().toString();
        final String password = "1213@123B";
        String[] tokenResponse;
        params.put("phone", "81898811");
        params.put("password", "1213@123B");

        JSONObject loginObject = new JSONObject(params);
        String param = loginObject.toString();

        HttpCall httpCallPost = new HttpCall();
        httpCallPost.setMethodtype(HttpCall.POST);
        httpCallPost.setUrl(util.LOGINAPIURL);

        httpCallPost.setParams(loginObject);
        new HttpRequests(){
            @Override
            public void onResponse(String response) {
                super.onResponse(response);
                Log.d(TAG,"JWT response: " + response);
                try {
                    String[] tokenResponse = JWTUtils.decoded(response);
                    Log.d(TAG,response);
                    Log.d(TAG, tokenResponse[0]);
                    Log.d(TAG, tokenResponse[1]);

                    JSONObject obj = new JSONObject(tokenResponse[1]);
                    Log.d(TAG, obj.getString("respond"));

                    String result = obj.getString("respond");
                    JSONObject respond = new JSONObject(result);
                    Log.d(TAG, respond.toString());

                    if(respond.getString("Success").equals("true")){
                        Intent otpIntent = new Intent(getApplicationContext(), OTPActivity.class);
                        otpIntent.putExtra("phone", phone);
                        otpIntent.putExtra("password", password);
                        startActivity(otpIntent);
                    }
                    else{
                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(httpCallPost);
    }
}
