package com.medos.mos.ui.login;

import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONObject;

import java.util.HashMap;

public class ForgetPassword extends AppCompatActivity {
    private static final String TAG = "ForgetPasswordActivity";
    HashMap<String, String> params = new HashMap<>();
    Utils util;
    String phone = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        util = new Utils();
        phone = getIntent().getStringExtra("phone");
    }

    public void changePasswordRequest(View view){
        EditText edCode = findViewById(R.id.edCode);
        EditText edNewPassword = findViewById(R.id.edNewPassword);
        final String code, newPassword;
        code = edCode.getText().toString();
        newPassword = edNewPassword.getText().toString();
        if(!code.equals("") || !newPassword.equals("")){
            String[] tokenResponse;
            params.put("forgetPasswordToken", code);
            params.put("password", newPassword);

            JSONObject loginObject = new JSONObject(params);

            HttpCall httpCallPost = new HttpCall();
            httpCallPost.setMethodtype(HttpCall.POST);
            httpCallPost.setUrl(util.FORGETAPIURL);

            httpCallPost.setParams(loginObject);
            new HttpRequests(this){
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
                            Intent resetIntent = new Intent(getApplicationContext(), LoginActivity.class);//
                            startActivity(resetIntent);
                        }
                        else{
                            Toast.makeText(ForgetPassword.this, "Wrong code reset", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ForgetPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute(httpCallPost);
        }

    }
}
