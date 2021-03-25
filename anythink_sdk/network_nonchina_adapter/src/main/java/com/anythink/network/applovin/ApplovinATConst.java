/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.applovin;

import com.applovin.sdk.AppLovinSdk;


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
