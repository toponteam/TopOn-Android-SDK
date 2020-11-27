/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.oneway;


import mobi.oneway.export.Ad.OnewaySdk;

public class OnewayATConst {
    public static final int NETWORK_FIRM_ID = 17;

    public static String getNetworkVersion() {
        try {
            return OnewaySdk.getVersion();
        } catch (Throwable e) {

        }
        return "";
    }

}
