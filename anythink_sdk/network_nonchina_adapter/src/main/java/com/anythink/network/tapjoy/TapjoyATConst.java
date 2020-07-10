package com.anythink.network.tapjoy;

import com.tapjoy.Tapjoy;

public class TapjoyATConst {
    public static final int NETWORK_FIRM_ID = 10;

    public static String getNetworkVersion() {
        try {
            return Tapjoy.getVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
