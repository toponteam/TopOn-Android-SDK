package com.anythink.network.maio;

import jp.maio.sdk.android.MaioAds;

public class MaioATConst {
    public static final int NETWORK_FIRM_ID = 24;

    public static String getNetworkVersion() {
        try {
            return MaioAds.getSdkVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
