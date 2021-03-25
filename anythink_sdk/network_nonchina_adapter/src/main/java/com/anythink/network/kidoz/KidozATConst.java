/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.network.kidoz;

import com.kidoz.sdk.api.KidozSDK;

public class KidozATConst {

    public static final int NETWORK_FIRM_ID = 45;

    public static String getNetworkVersion() {
        try {
            return KidozSDK.getSDKVersion();
        } catch (Throwable e) {
        }
        return "";
    }
}
