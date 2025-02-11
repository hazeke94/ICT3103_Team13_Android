package com.medos.mos;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.URL;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;


/**
 * Created by pethoalpar on 4/16/2016.
 */
public class HttpRequests extends AsyncTask<HttpCall, String, String>{

    private static final String UTF_8 = "UTF-8";
    private static String TAG = "HttpRequests";
    private ProgressDialog dialog;


    public HttpRequests(Activity activity){
        dialog = new ProgressDialog(activity);
        dialog.setCancelable(false);
    }
    @Override
    protected void onPreExecute() {
        dialog.setMessage("Please wait..");
        dialog.show();
    }
    @Override
    protected String doInBackground(HttpCall... params) {
        HttpURLConnection urlConnection = null;
        HttpCall httpCall = params[0];
        String token = httpCall.getHeader();
        StringBuilder response = new StringBuilder();
        try{
            String dataParams = "";
            if(httpCall.getMethodtype() == HttpCall.GET){
                if(httpCall.getParams()!= null){
                    Log.d(TAG,"not null");
                    JSONObject obj = new JSONObject(httpCall.getParams().toString());
                    if(obj.has("StartDate")) {
                        dataParams = obj.getString("StartDate");
                    }
                    else if(obj.has("MedicalBookingID")){
                        dataParams = obj.getString("MedicalBookingID");
                    }
                }

            }
            else{
                dataParams = httpCall.getParams().toString();
            }

            Log.d(TAG, dataParams);
            byte[] postData = dataParams.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            URL url = new URL(httpCall.getMethodtype() == HttpCall.GET ? httpCall.getUrl() + dataParams : httpCall.getUrl());
            Log.d(TAG,url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(httpCall.getMethodtype() == HttpCall.GET ? "GET":"POST");
            if(httpCall.getMethodtype() == HttpCall.POST){
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            }

            if(token!= null){
                urlConnection.setRequestProperty("token", token);
                Log.d(TAG,"token : " + token);
            }

            urlConnection.setReadTimeout(60000 /* milliseconds */);
            urlConnection.setConnectTimeout(60000 /* milliseconds */);
            if(httpCall.getParams() != null && httpCall.getMethodtype() == HttpCall.POST){
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.write(postData);
                wr.flush();
                wr.close();
            }
            Log.d(TAG,"get Response");
            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG, String.valueOf(responseCode));
            if(responseCode == HttpURLConnection.HTTP_OK){
                String line ;
                BufferedReader br = new BufferedReader( new InputStreamReader(urlConnection.getInputStream()));
                while ((line = br.readLine()) != null){
                    Log.d(TAG,line);
                    response.append(line);
                }
            }
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG,"UnsupportedEncodingException");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.d(TAG,"MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG,"IOException");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d(TAG,"JSONException");
            e.printStackTrace();
        } finally {
            if(urlConnection !=null) {
                urlConnection.disconnect();
            }
        }
        return response.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        onResponse(s);
    }

    public void onResponse(String response){

    }


}