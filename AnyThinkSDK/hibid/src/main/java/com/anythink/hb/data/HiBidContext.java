package com.anythink.hb.data;

import android.content.Context;

public class HiBidContext {
    private Context context;
    private String appId;
    private String appKey;

    public HiBidContext(Context context, String appId, String appKey){
        this.context = context;
        this.appId = appId;
        this.appKey = appKey;
    }

    public Context getContext() {
        return context;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppKey() {
        return appKey;
    }
}
