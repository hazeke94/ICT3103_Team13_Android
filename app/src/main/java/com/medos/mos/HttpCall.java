package com.medos.mos;
import org.json.JSONObject;

/**
 * Created by pethoalpar on 4/16/2016.
 */
public class HttpCall {

    public static final int GET = 1;
    public static final int POST = 2;

    private String url;
    private int methodtype;
    private JSONObject params;
    private String header;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMethodtype() {
        return methodtype;
    }

    public void setMethodtype(int methodtype) {
        this.methodtype = methodtype;
    }

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}