/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ksyun;

import com.ksc.ad.sdk.KsyunAdSdk;

public class KsyunATConst {
    public static final int NETWORK_FIRM_ID = 19;

    public static String getNetworkVersion() {
        try {
            return KsyunAdSdk.getInstance().getSdkVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
