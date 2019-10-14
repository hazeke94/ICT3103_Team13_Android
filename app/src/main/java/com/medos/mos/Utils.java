package com.medos.mos;

import com.medos.mos.model.Payload;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    public String LOGINAPIURL = "https://ict3103api.azurewebsites.net/api/Login";
    public String OTPAPIURL = "https://ict3103api.azurewebsites.net/api/Otp";
    public String MedicalGETURL = "https://ict3103api.azurewebsites.net/api/ClinicBookingHours/MedicalAppointment?StartDate=";

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
        Payload payloadObj = new com.medos.mos.model.Payload(url,unixTime + 10,unixTime);
        return payloadObj;

    }
}
