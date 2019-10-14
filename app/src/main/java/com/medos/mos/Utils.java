package com.medos.mos;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    public String LOGINAPIURL = "https://ict3103api.azurewebsites.net/api/Login";
    public String OTPAPIURL = "https://ict3103api.azurewebsites.net/api/Otp";

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
}
