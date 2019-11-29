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
import java.util.regex.Pattern;

public class ForgetPassword extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[!@#$%^&*()_+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");

    private static final String TAG = "ForgetPasswordActivity";
    HashMap<String, String> params = new HashMap<>();
    Utils util;
    String phone = "";

    public static native String getRSAKey();

    static {
        System.loadLibrary("button-lib");
    }

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

        if (!validatePassword(newPassword)) { //added
            return;
        }

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
                            Toast.makeText(ForgetPassword.this, "Invalid Reset Code", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ForgetPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute(httpCallPost);
        }
    }
    
    public static final String enRsaKey = getRSAKey();

    private boolean validatePassword(String newPassword) { //added
        //String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (newPassword.isEmpty()) {
            Toast.makeText(ForgetPassword.this, "Please enter a new password", Toast.LENGTH_SHORT).show();
            //textInputPassword.setError("Field can't be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            Toast.makeText(ForgetPassword.this, "Password must contain be at least 8 characters long and must include the following:\n 1 lowercase\n 1 uppercase\n 1 number\n 1 special character", Toast.LENGTH_SHORT).show();

            //textInputPassword.setError("Password too weak");
            return false;
        } else {
            ///textInputPassword.setError(null);
            return true;
        }
    }

}
