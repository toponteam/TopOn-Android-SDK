package com.anythink.network.chartboost;

import com.chartboost.sdk.Chartboost;

public class ChartboostATConst {
    public static final int NETWORK_FIRM_ID = 9;

    public static String getNetworkVersion() {
        try {
            return Chartboost.getSDKVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
