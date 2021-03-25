/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.fyber;

import com.fyber.inneractive.sdk.external.InneractiveAdManager;

public class FyberATConst {

    public static final int NETWORK_FIRM_ID = 37;

    public static String getNetworkVersion() {
        try {
            return InneractiveAdManager.getVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
