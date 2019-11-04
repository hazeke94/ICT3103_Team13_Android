package com.medos.mos.ui.login;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.medos.mos.AES_ECB;
import com.medos.mos.HttpCall;
import com.medos.mos.HttpRequests;
import com.medos.mos.MainActivity;
import com.medos.mos.R;
import com.medos.mos.Utils;
import com.medos.mos.ui.JWTUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

public class OTPActivity extends AppCompatActivity {
    String phone;
    String password;
    Utils util;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static final String TAG = "OTPActivity";
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String ALIAS = "userSession";
    private static final String TYPE_RSA = "RSA";
    private static final String CYPHER = "RSA/ECB/PKCS1Padding";
    private static final String ENCODING = "UTF-8";

    @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendOTP(View view) {
        EditText edOTP = findViewById(R.id.edOTP);
        int otp_input = Integer.parseInt(edOTP.getText().toString());
        final Context context = this;

        //TAO:
        boolean hasRSK = pref.contains("rsk");
        if (hasRSK == false) {
            Log.d(TAG, "RSK DONT EXIST");
            editor.putString("rsk", encryptString(context, AES_ECB.getEnRsaKey()));
            //editor.putString("rsk", AES_ECB.getEnRsaKey());
            editor.apply();
            {Log.d(TAG, "RSK INSERTED");}
        } else {Log.d(TAG, "RSK EXISTS");}


        try {
            //TAO
            Log.d(TAG, "Finding SPIK");
            String enRsaKey = decryptString(context, pref.getString("rsk", ""));
            String rsaKey = AES_ECB.getRsaKey(enRsaKey);
            String SPIK = AES_ECB.decryptRsa(rsaKey);

            String token = util.generateToken(SPIK, getResources().getString(R.string.issuer));

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

                            editor.putString("sessionToken", encryptString(context, resObj.getString("sessiontoken")));
                            editor.putString("Phone", encryptString(context, phone));
                            editor.putString("Password", encryptString(context, password));
                            editor.putString("LoginTimeStamp", encryptString(context, String.valueOf(loginTimeStamp)));

                            editor.apply();
                            Log.d(TAG, "Timestamp AFTER Encryption: " + encryptString(context, String.valueOf(loginTimeStamp)));

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

    // Encrypt the user login data
    public static String encryptString(Context context, String toEncrypt) {
        try {
            final KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKey(context);
            if (privateKeyEntry != null) {
                final PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();

                // Encrypt the text
                Cipher input = Cipher.getInstance(CYPHER);
                input.init(Cipher.ENCRYPT_MODE, publicKey);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, input);
                cipherOutputStream.write(toEncrypt.getBytes(ENCODING));
                cipherOutputStream.close();

                byte[] vals = outputStream.toByteArray();

                return Base64.encodeToString(vals, Base64.DEFAULT);

            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }

        return null;
    }

    // Decrypt the encrypted string to get cleartext
    public static String decryptString(Context context, String toDecrypt) {
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = getPrivateKey(context);
            if (privateKeyEntry != null) {
                final PrivateKey privateKey = privateKeyEntry.getPrivateKey();

                Cipher output = Cipher.getInstance(CYPHER);
                output.init(Cipher.DECRYPT_MODE, privateKey);

                CipherInputStream cipherInputStream = new CipherInputStream(
                        new ByteArrayInputStream(Base64.decode(toDecrypt, Base64.DEFAULT)), output);
                ArrayList<Byte> values = new ArrayList<>();
                int nextByte;
                while ((nextByte = cipherInputStream.read()) != -1) {
                    values.add((byte) nextByte);
                }

                byte[] bytes = new byte[values.size()];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = values.get(i);
                }

                return new String(bytes, 0, bytes.length, ENCODING);
            }
        } catch (Exception e) {
            Log.w(TAG, Log.getStackTraceString(e));
        }

        return null;
    }


    private static KeyStore.PrivateKeyEntry getPrivateKey(Context context) throws KeyStoreException,
            UnrecoverableEntryException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore ks = KeyStore.getInstance(KEYSTORE);

        // Need to call "load", or it'll crash
        ks.load(null);

        // Load the key pair from the Android Key Store
        KeyStore.Entry entry = ks.getEntry(ALIAS, null);

        // If the entry is null, keys were never stored under this alias
        if (entry == null) {
            Log.w(TAG, "No key found under alias: " + ALIAS);
            Log.w(TAG, "Generating new key...");

            try {
                createKeys(context);

                // reload keystore
                ks = KeyStore.getInstance(KEYSTORE);
                ks.load(null);

                // reload keypair
                entry = ks.getEntry(ALIAS, null);

                if (entry == null) {
                    Log.w(TAG, "Generating new key failed...");
                    return null;
                }
            } catch (InvalidAlgorithmParameterException e) {
                Log.w(TAG, "Generating new key failed...");
                e.printStackTrace();
                return null;
            } catch (NoSuchProviderException e) {
                Log.w(TAG, "Generating new key failed...");
                e.printStackTrace();
                return null;
            }
        }

        /* If entry is not a KeyStore.PrivateKeyEntry, it might have gotten stored in a previous
         * iteration of the application that was using some other mechanism, or been overwritten
         * by something else using the same keystore with the same alias.
         * Can determine the type using entry.getClass() and debug from there.
         * */
        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            Log.w(TAG, "Not an instance of a PrivateKeyEntry.");
            Log.w(TAG, "Exiting signData()");
            return null;
        }

        return (KeyStore.PrivateKeyEntry) entry;
    }

    /*
     * Creates a public and private key and stores it using the Android Key Store,
     * so that only this application will be able to access the keys
     */
    private static void createKeys(Context context) throws NoSuchProviderException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException {
        // Create a start and end time, for the validity range of the key pair that's about to be generated
        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 25);

        // The KeyPairGeneratorSpec object is how parameters for the key pair are passed
        // to the KeyPairGenerator
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(ALIAS) // A key for the key
                .setSubject(new X500Principal("CN=" + ALIAS)) // Subject used for the self-signed certificate of the generated pair
                .setSerialNumber(BigInteger.valueOf(1337)) // Serial number used for the self-signed certificate of the generated pair
                // Date range of validity for the generated pair
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        // Initialize a KeyPair generator using the intended algorithm
        final KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(TYPE_RSA, KEYSTORE);
        kpGenerator.initialize(spec);

        final KeyPair kp = kpGenerator.generateKeyPair();
        //Log.d(TAG, "Public key is: " + kp.getPublic().toString());
    }
}
