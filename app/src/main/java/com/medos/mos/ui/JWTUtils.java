package com.medos.mos.ui;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class JWTUtils {

    public static String[] decoded(String JWTEncoded) throws Exception {
        String[] result = new String[2];
        try {
            String[] split = JWTEncoded.split("\\.");
            result[0] = getJson(split[0]);
            result[1] = getJson(split[1]);
            return result;
        } catch (UnsupportedEncodingException e) {
            //Error
            return null;
        }
    }

    public static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

    public static PrivateKey generatePrivateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] encodedKey = android.util.Base64.decode(key, android.util.Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);

        return privKey;
    }
}