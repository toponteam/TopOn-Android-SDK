package com.anythink.network.inmobi;

import com.inmobi.sdk.InMobiSdk;

/**
 * Created by zhou on 2018/7/4.
 */

public class InmobiATConst {
    public static final int NETWORK_FIRM_ID = 3;

    public static String getNetworkVersion() {
        try {
            return InMobiSdk.getVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
