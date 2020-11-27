package com.anythink.network.ironsource;

import com.ironsource.sdk.mediation.BuildConfig;

public class IronsourceATConst {
    public static final int NETWORK_FIRM_ID = 11;

    public static String getNetworkVersion() {
        try {
            return BuildConfig.VERSION_NAME;
        } catch (Throwable e) {
        }
        return "";
    }
}
