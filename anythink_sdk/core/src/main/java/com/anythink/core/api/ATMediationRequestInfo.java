package com.anythink.core.api;

import java.util.Map;

public abstract class ATMediationRequestInfo {
    protected int networkFirmId;
    protected String className;

    protected String adSourceId;

    public int getNetworkFirmId() {
        return networkFirmId;
    }

    public String getClassName() {
        return className;
    }

    public String getAdSourceId() {
        return adSourceId;
    }

    public void setAdSourceId(String adSourceId) {
        this.adSourceId = adSourceId;
    }

    public abstract void setFormat(String format);

    public abstract Map<String, Object> getRequestParamMap();

}
