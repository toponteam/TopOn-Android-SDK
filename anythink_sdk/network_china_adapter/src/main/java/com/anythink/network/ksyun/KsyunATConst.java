package com.anythink.network.ksyun;

import com.ksc.ad.sdk.KsyunAdSdk;

public class KsyunATConst {
    public static final int NETWORK_FIRM_ID = 19;

    public static String getNetworkVersion() {
        try {
            return KsyunAdSdk.getInstance().getSdkVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
