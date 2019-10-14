package com.medos.mos;
import android.os.AsyncTask;
import android.util.Log;

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

    @Override
    protected String doInBackground(HttpCall... params) {
        HttpURLConnection urlConnection = null;
        HttpCall httpCall = params[0];
        String token = httpCall.getHeader();
        StringBuilder response = new StringBuilder();
        try{
            String dataParams = httpCall.getParams().toString();
            Log.d(TAG, dataParams);
            byte[] postData = dataParams.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            URL url = new URL(httpCall.getMethodtype() == HttpCall.GET ? httpCall.getUrl() + dataParams : httpCall.getUrl());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(httpCall.getMethodtype() == HttpCall.GET ? "GET":"POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            urlConnection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            if(token!= null){
                urlConnection.setRequestProperty("token", token);
                Log.d(TAG,"token : " + token);
            }

            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            if(httpCall.getParams() != null && httpCall.getMethodtype() == HttpCall.POST){
//                OutputStream os = urlConnection.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, UTF_8));
//                Log.d(TAG, dataParams);
//                writer.append(dataParams);
//                writer.flush();
//                writer.close();
//                os.close();
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.write(postData);
                wr.flush();
                wr.close();
            }
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
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            urlConnection.disconnect();
        }
        return response.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        onResponse(s);
    }

    public void onResponse(String response){

    }


}