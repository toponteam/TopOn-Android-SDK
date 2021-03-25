/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

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
