package com.medos.mos;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.medos.mos.model.Payload;
import com.medos.mos.ui.JWTUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public String LOGINAPIURL = "https://ict3103api.azurewebsites.net/api/Login";
    public String OTPAPIURL = "https://ict3103api.azurewebsites.net/api/Otp";
    public String AvailableSlotsGETURL = "https://ict3103api.azurewebsites.net/api/ClinicBookingHours/MedicalAppointment?StartDate=";
    public String MEDICALAPPTURL = "https://ict3103api.azurewebsites.net/api/MedicalAppointment";
    public String FORGETREQUESTAPIURL = "https://ict3103api.azurewebsites.net/api/ForgetPassword/Request";
    public String FORGETAPIURL = "https://ict3103api.azurewebsites.net/api/ForgetPassword";


    public String removeResponse(String payload){
        JSONObject payloadObj;
        try {
            payloadObj = new JSONObject(payload);
            payloadObj.remove("respond");

            return payloadObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public Payload generatePayload (String url) {

        long unixTime = System.currentTimeMillis() / 1000;
        Payload payloadObj = new com.medos.mos.model.Payload(url,unixTime + 60,unixTime);
        return payloadObj;

    }

    public String generateToken(String str, String issuer){
        Map<String, Object> headerClaims = new HashMap();
        headerClaims.put("alg", "RS256");
        headerClaims.put("typ", "JWT");
        Payload payload = generatePayload(issuer);

        String privateKey = str;
        privateKey = privateKey.replace("-----BEGIN RSA PRIVATE KEY-----", "");
        privateKey = privateKey.replace("-----END RSA PRIVATE KEY-----", "");
        privateKey = privateKey.replaceAll("\\s+", "");

        PrivateKey privKey = null;
        try {
            privKey = JWTUtils.generatePrivateKey(privateKey);
            Algorithm algorithm = Algorithm.RSA256(null, (RSAPrivateKey) privKey);

            //create token to be sent for otp
            String token = JWT.create()
                    .withHeader(headerClaims)
                    .withClaim("iss", payload.getIss())
                    .withClaim("exp", payload.getEx())
                    .withClaim("iat", payload.getIat())
                    .sign(algorithm);

            return token;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String generateToken(String str, String issuer, String t){
        Map<String, Object> headerClaims = new HashMap();
        headerClaims.put("alg", "RS256");
        headerClaims.put("typ", "JWT");
        Payload payload = generatePayload(issuer);

        String privateKey = str;
        privateKey = privateKey.replace("-----BEGIN RSA PRIVATE KEY-----", "");
        privateKey = privateKey.replace("-----END RSA PRIVATE KEY-----", "");
        privateKey = privateKey.replaceAll("\\s+", "");

        PrivateKey privKey = null;
        try {
            privKey = JWTUtils.generatePrivateKey(privateKey);
            Algorithm algorithm = Algorithm.RSA256(null, (RSAPrivateKey) privKey);

            //create token to be sent for otp
            String token = JWT.create()
                    .withHeader(headerClaims)
                    .withClaim("iss", payload.getIss())
                    .withClaim("exp", payload.getEx())
                    .withClaim("iat", payload.getIat())
                    .withClaim("token", t)
                    .sign(algorithm);

            return token;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }
}
