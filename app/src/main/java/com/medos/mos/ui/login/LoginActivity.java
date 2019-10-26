package com.medos.mos.ui.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.medos.mos.MainActivity;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.ui.JWTUtils;
import com.medos.mos.ui.medicalAppointment.medicalAppointmentFragment;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;


public class LoginActivity extends AppCompatActivity {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String ALIAS = "userSession";
    private static final String TYPE_RSA = "RSA";
    private static final String CYPHER = "RSA/ECB/PKCS1Padding";
    private static final String ENCODING = "UTF-8";
    private static final String TAG = "LoginActivity";
    Utils util;
    HashMap<String, String> params = new HashMap<>();
    SharedPreferences pref;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        util = new Utils();
        pref = getApplicationContext().getSharedPreferences("Session", 0); // 0 - for private mode
        String p = pref.getString("Phone", "");
        Log.d(TAG, p);
        context = this;
        if(p != ""){
            //redirect to mainactivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void login(View view) {
        EditText edPhone = findViewById(R.id.editTextPhoneNumber);
        EditText edPassword = findViewById(R.id.editTextPassword);

        final String phone = edPhone.getText().toString();
        final String password = edPassword.getText().toString();

        boolean validate = util.validateNumber(phone);
        if(validate){
            String[] tokenResponse;
            params.put("phone", phone);
            params.put("password", password);
            JSONObject loginObject = new JSONObject(params);

            HttpCall httpCallPost = new HttpCall();
            httpCallPost.setMethodtype(HttpCall.POST);
            httpCallPost.setUrl(util.LOGINAPIURL);

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
        else{
            Toast.makeText(context, "Enter a valid number", Toast.LENGTH_SHORT).show();
        }

    }

    public void forgetPassword(View view) {
        if (pref.getString("phone", "") != "") {
            params = new HashMap<>();
            params.put("phone", "81898811");
            JSONObject resetObject = new JSONObject(params);

            HttpCall httpCallPost = new HttpCall();
            httpCallPost.setMethodtype(HttpCall.POST);
            httpCallPost.setUrl(util.FORGETREQUESTAPIURL);

            httpCallPost.setParams(resetObject);
            new HttpRequests(this) {
                @Override
                public void onResponse(String response) {
                    super.onResponse(response);
                    Log.d(TAG, "JWT response: " + response);
                }
            }.execute(httpCallPost);
        } else {
//            Toast.makeText(this, "No login session detected.", Toast.LENGTH_SHORT).show();
            //open dialog to confirm
            final EditText input = new EditText(this);
            input.setHint("Phone Number");
            input.setText(pref.getString("Phone",""));
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Reset Password");
            alertDialog.setMessage("Is this your number? Do you want to reset");
            alertDialog.setView(input);

            alertDialog.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //generate token first
                    //validate number
                    boolean validate = util.validateNumber(input.getText().toString());
                    if(validate){
                        JSONObject resetObj = new JSONObject();
                        try {
                            resetObj.put("phone", input.getText().toString());
                            HttpCall httpCallPost = new HttpCall();
                            httpCallPost.setMethodtype(HttpCall.POST);
                            httpCallPost.setUrl(util.FORGETREQUESTAPIURL);

                            httpCallPost.setParams(resetObj);
                            new HttpRequests((Activity) context) {
                                @Override
                                public void onResponse(String response) {
                                    super.onResponse(response);
                                    Log.d(TAG, "JWT response: " + response);
                                    try {
                                        String[] tokenResponse = JWTUtils.decoded(response);
                                        JSONObject obj = new JSONObject(tokenResponse[1]);

                                        String result = obj.getString("respond");
                                        Log.d(TAG, result);

                                        JSONObject respond = new JSONObject(result);
                                        if(respond.getString("Success").equals("true")){
                                            Intent intent = new Intent(LoginActivity.this, ForgetPassword.class);
                                            intent.putExtra("phone", input.getText().toString());
                                            startActivity(intent);
                                        }
                                        else{
                                            Toast.makeText(LoginActivity.this, "Error in requesting for reset", Toast.LENGTH_SHORT).show();
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
                    else{

                    }

                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // DO SOMETHING HERE
                    dialog.cancel();
                }
            });

            AlertDialog dialog = alertDialog.create();
            dialog.show();
        }
    }
}
