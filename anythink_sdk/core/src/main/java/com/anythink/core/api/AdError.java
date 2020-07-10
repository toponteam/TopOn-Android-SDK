package com.anythink.core.api;

/**
 * Created by zhou on 2018/1/13.
 */

public class AdError {
    /***
     * Error Code
     */
    protected String code;
    /***
     * Error Message
     */
    protected String desc;

    /**
     * Mediation Error Info
     */
    protected String platformCode;
    protected String platformMSG;

    protected AdError(String code, String desc, String platformCode, String platformMSG) {
        this.code = code;
        this.desc = desc;
        this.platformCode = platformCode;
        this.platformMSG = platformMSG;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public String getPlatformMSG() {
        return platformMSG;
    }

    public String printStackTrace() {
        return "code[ " + code + " ],desc[ " + desc + " ],platformCode[ " + platformCode + " ],platformMSG[ " + platformMSG + " ]";
    }
}
