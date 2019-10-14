package com.medos.mos.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.model.Payload;
import com.medos.mos.ui.JWTUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class OTPActivity extends AppCompatActivity {
    String phone = "";
    Utils util;

    private static final String TAG = "OTPActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        phone = getIntent().getStringExtra("phone");
        util = new Utils();
    }

    public void sendOTP(View view) {
        EditText edOTP = findViewById(R.id.edOTP);
        Payload payload;
        int otp_input = Integer.parseInt(edOTP.getText().toString());


        Map<String, Object> headerClaims = new HashMap();
        headerClaims.put("alg", "RS256");
        headerClaims.put("typ", "JWT");

        //generate Payload
        payload = generatePayload();

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
                    .sign(algorithm);
            Log.d(TAG,token);
            Log.d(TAG,algorithm.toString());
            JSONObject otp_submit = new JSONObject();
//            otp_submit.put("otp", otp_input);
            otp_submit.put("otp", otp_input);
            otp_submit.put("phone", phone);

            HttpCall httpCallPost = new HttpCall();
            httpCallPost.setHeader(token);
            httpCallPost.setMethodtype(HttpCall.POST);
            httpCallPost.setUrl(util.OTPAPIURL);

            httpCallPost.setParams(otp_submit);
            new HttpRequests() {
                @Override
                public void onResponse(String response) {
                    super.onResponse(response);
                    Log.d(TAG, "JWT response: " + response);
                }
            }.execute(httpCallPost);


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (InvalidKeySpecException ex) {
            ex.printStackTrace();
        }
    }




    public Payload generatePayload () {

        long unixTime = System.currentTimeMillis() / 1000;
        Payload payloadObj = new com.medos.mos.model.Payload(getResources().getString(R.string.issuer),unixTime + 10,unixTime);
        return payloadObj;

    }



}
