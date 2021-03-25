/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ogury;

import com.ogury.sdk.Ogury;

public class OguryATConst {
    public static final int NETWORK_FIRM_ID = 36;

    public static String getNetworkVersion() {
        try {
            return Ogury.getSdkVersion();
        } catch (Exception e) {

        }
        return "";
    }
}
