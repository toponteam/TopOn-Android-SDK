package com.anythink.network.applovin;

import com.applovin.sdk.AppLovinSdk;

/**
 * Created by zhou on 2018/7/4.
 */

public class ApplovinATConst {
    public static final int NETWORK_FIRM_ID = 5;

    public static String getNetworkVersion() {
        try {
            return AppLovinSdk.VERSION;
        } catch (Throwable e) {

        }
        return "";
    }
}
