package com.anythink.network.facebook;

import com.facebook.ads.internal.api.BuildConfigApi;

public class FacebookATConst {
    public static final int NETWORK_FIRM_ID = 1;

    public static String getNetworkVersion() {
        try {
            return BuildConfigApi.getVersionName(null);
        } catch (Throwable e) {

        }
        return "";
    }
}
