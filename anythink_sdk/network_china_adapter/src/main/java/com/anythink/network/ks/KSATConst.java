/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ks;


import com.kwad.sdk.api.KsAdSDK;

public class KSATConst {
    public static final int NETWORK_FIRM_ID = 28;
    public static final String REWARDEDVIDEO_SKIP_AFTER_THIRTY_SECOND = "KS_RV_SKIP_AFTER_THIRTY_SECOND";

    public static String getSDKVersion() {
        try {
            return KsAdSDK.getSDKVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
