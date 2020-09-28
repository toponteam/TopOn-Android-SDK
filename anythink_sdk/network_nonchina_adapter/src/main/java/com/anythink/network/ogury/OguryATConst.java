package com.anythink.network.ogury;

import io.presage.common.PresageSdk;

public class OguryATConst {
    public static final int NETWORK_FIRM_ID = 36;

    public static String getSDKVersion() {
        try {
            return PresageSdk.getAdsSdkVersion();
        } catch (Exception e) {

        }
        return "";
    }
}
