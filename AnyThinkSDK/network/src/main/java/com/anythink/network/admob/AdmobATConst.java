package com.anythink.network.admob;

import com.google.android.gms.ads.MobileAds;

/**
 * Created by zhou on 2018/7/4.
 */

public class AdmobATConst {
    public static final int NETWORK_FIRM_ID = 2;

    public static String getNetworkVersion() {
        try {
            return MobileAds.getVersionString();
        } catch (Throwable e) {

        }
        return "";
    }
}
