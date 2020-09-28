package com.anythink.hb.adx.network;

import org.json.JSONObject;

public abstract class BaseNetworkInfo {
    String appId;
    String unitId;
    int firmId;
    String buyerUid;
    String format;


    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public int getFirmId() {
        return firmId;
    }

    public void setFirmId(int firmId) {
        this.firmId = firmId;
    }

    public String getBuyerUid() {
        return buyerUid;
    }

    public void setBuyerUid(String buyerUid) {
        this.buyerUid = buyerUid;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public abstract String getSDKVersion();

    public abstract boolean checkNetworkSDK();

    public abstract JSONObject toRequestJSONObject();
}
