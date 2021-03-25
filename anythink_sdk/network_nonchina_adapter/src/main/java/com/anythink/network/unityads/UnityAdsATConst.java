/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.unityads;

import com.unity3d.ads.UnityAds;

public class UnityAdsATConst {
    public static final int NETWORK_FIRM_ID = 12;

    public static String getNetworkVersion() {
        try {
            return UnityAds.getVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
