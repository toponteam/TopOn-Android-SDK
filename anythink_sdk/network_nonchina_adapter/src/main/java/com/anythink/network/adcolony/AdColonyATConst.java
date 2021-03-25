/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.adcolony;

import com.adcolony.sdk.AdColony;

public class AdColonyATConst {
    public static final int NETWORK_FIRM_ID = 14;

    public static String getNetworkVersion() {
        try {
            return AdColony.getSDKVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
