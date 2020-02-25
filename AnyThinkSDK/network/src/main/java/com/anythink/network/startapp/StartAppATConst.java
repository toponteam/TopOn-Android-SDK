package com.anythink.network.startapp;

import com.startapp.android.publish.GeneratedConstants;

public class StartAppATConst {
    public static final int NETWORK_FIRM_ID = 25;

    public static String getSDKVersion() {
        try {
            return GeneratedConstants.INAPP_VERSION;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }
}
