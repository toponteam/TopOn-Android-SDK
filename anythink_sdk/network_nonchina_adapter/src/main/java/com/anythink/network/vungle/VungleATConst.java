/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.vungle;

import com.vungle.warren.BuildConfig;

public class VungleATConst {
    public static final int NETWORK_FIRM_ID = 13;

    public static String getNetworkVersion() {
        try {
            return BuildConfig.VERSION_NAME;
        } catch (Throwable e) {
        }
        return "";
    }
}
