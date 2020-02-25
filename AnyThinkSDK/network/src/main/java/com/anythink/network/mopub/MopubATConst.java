package com.anythink.network.mopub;

import com.mopub.common.MoPub;

/**
 * Created by zhou on 2018/7/4.
 */

public class MopubATConst {
    public static final int NETWORK_FIRM_ID = 7;

    public static String getNetworkVersion() {
        try {
            return MoPub.SDK_VERSION;
        } catch (Throwable e) {

        }
        return "";
    }
}
