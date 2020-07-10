package com.anythink.network.adcolony;

import com.adcolony.sdk.AdColony;

public class AdColonyATConst {
    public static final int NETWORK_FIRM_ID = 14;

    public static String getNetworkVersion() {
        try {
            return AdColony.getSDKVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
