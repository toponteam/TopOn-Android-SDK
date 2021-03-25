/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.startapp;

import com.startapp.sdk.adsbase.StartAppSDK;

public class StartAppATConst {
    public static final int NETWORK_FIRM_ID = 25;

    public static String getNetworkVersion() {
        try {
            return StartAppSDK.getVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }
}
