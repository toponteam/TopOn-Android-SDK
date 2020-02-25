package com.anythink.network.sigmob;

import com.sigmob.windad.WindAds;

public class SigmobATConst {
    public static final int NETWORK_FIRM_ID = 29;

    public static String getSDKVersion() {
        try {
            return WindAds.getVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }
}
