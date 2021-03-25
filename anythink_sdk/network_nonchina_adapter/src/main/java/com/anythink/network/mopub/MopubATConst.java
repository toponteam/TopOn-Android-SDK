/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.mopub;

import com.mopub.common.MoPub;


public class MopubATConst {
    public static final int NETWORK_FIRM_ID = 7;

    public static String getNetworkVersion() {
        try {
            return MoPub.SDK_VERSION;
        } catch (Throwable e) {

        }
        return "";
    }
}
