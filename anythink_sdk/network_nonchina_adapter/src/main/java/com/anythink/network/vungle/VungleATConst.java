package com.anythink.network.vungle;

import com.vungle.warren.BuildConfig;

public class VungleATConst {
    public static final int NETWORK_FIRM_ID = 13;

    public static String getNetworkVersion() {
        try {
            return BuildConfig.VERSION_NAME;
        } catch (Throwable e) {
        }
        return "";
    }
}
