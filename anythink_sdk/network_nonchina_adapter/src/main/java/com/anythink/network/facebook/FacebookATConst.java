package com.anythink.network.facebook;

import com.facebook.ads.BuildConfig;

public class FacebookATConst {
    public static final int NETWORK_FIRM_ID = 1;

    public static String getNetworkVersion() {
        try {
            return BuildConfig.VERSION_NAME;
        } catch (Throwable e) {

        }
        return "";
    }
}
