package com.medos.mos.model;

public class Payload {
    String iss;
    long ex,iat;

    public Payload(String iss, long ex, long iat) {
        this.iss = iss;
        this.ex = ex;
        this.iat = iat;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public long getEx() {
        return ex;
    }

    public void setEx(long ex) {
        this.ex = ex;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    @Override
    public String toString() {
        return
                "iss:'" + iss + '\'' +
                ", ex:" + ex + '\'' +
                ", iat:" + iat;
    }
}
