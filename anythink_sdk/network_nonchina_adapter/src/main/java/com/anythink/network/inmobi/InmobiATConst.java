/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.inmobi;

import com.inmobi.sdk.InMobiSdk;


public class InmobiATConst {
    public static final int NETWORK_FIRM_ID = 3;

    public static String getNetworkVersion() {
        try {
            return InMobiSdk.getVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
