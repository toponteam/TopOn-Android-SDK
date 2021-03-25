/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.chartboost;

import com.chartboost.sdk.Chartboost;

public class ChartboostATConst {
    public static final int NETWORK_FIRM_ID = 9;

    public static String getNetworkVersion() {
        try {
            return Chartboost.getSDKVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
