/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.maio;

import jp.maio.sdk.android.MaioAds;

public class MaioATConst {
    public static final int NETWORK_FIRM_ID = 24;

    public static String getNetworkVersion() {
        try {
            return MaioAds.getSdkVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
