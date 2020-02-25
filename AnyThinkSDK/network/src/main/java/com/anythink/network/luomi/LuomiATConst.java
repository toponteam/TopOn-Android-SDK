package com.anythink.network.luomi;

import com.hz.yl.LibCfg;

public class LuomiATConst {
    public static final int NETWORK_FIRM_ID = 27;

    public static String getSDKVersion() {
        try {
            return LibCfg.SDKVER;
        } catch (Exception e) {

        }
        return "";
    }
}
