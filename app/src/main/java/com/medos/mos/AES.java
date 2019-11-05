package com.medos.mos;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.medos.mos.ui.login.ForgetPassword;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class AES {

        private static SecretKeySpec secretKey;
        private static byte[] key;

        public static native String getRSA();
        public static native String getFKey();

        static {
            System.loadLibrary("api-keys");
        }

        //DONE: hide both with ndk
        private static final String enRsa = getRSA();

    public static void setKey(String myKey)
        {
            MessageDigest sha = null;
            try {
                key = myKey.getBytes("UTF-8");
                sha = MessageDigest.getInstance("SHA-1");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                secretKey = new SecretKeySpec(key, "AES");
            }
            catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        @RequiresApi(api = Build.VERSION_CODES.O)
        public static String decryptRsa(String secret)
        {
            try
            {
                setKey(secret);
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                String x = new String(cipher.doFinal(Base64.getDecoder().decode(enRsa)));
                System.out.println("HERE: " + x);
                return new String(cipher.doFinal(Base64.getDecoder().decode(enRsa)));
            }
            catch (Exception e)
            {
                System.out.println("Error while decrypting: " + e.toString());
            }
            return null;
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decryptRsaKey(String strToDecrypt, String secret)
    {
        try
        {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (Exception e)
        {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getFirstKey(){
            //DONE: hide with ndk

            // TO BE REMOVED:
            //String[] firstKeyMix2 = {"y","s","Z","Q","3","W","c","O","W","w","f","6","T","S","f","H","l","b"};
            //String firstKey = String.join("", firstKeyMix2);

            return getFKey();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getRsaKey(String key){ // use this at all methods to get key
            String firstKey = getFirstKey();
            String rsaKey = decryptRsaKey(key, firstKey);
            return rsaKey;
    }

    public static String getEnRsaKey(){
        return ForgetPassword.enRsaKey;
    }
}
