/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.admob;


public class AdmobATConst {
    public static final int NETWORK_FIRM_ID = 2;

    public static final String ADAPTIVE_TYPE = "adaptive_type";
    public static final String ADAPTIVE_ORIENTATION = "adaptive_orientation";
    public static final String ADAPTIVE_WIDTH = "adaptive_width";

    public static final int ADAPTIVE_ANCHORED = 0;
    public static final int ADAPTIVE_INLINE = 1;

    public static final int ORIENTATION_CURRENT = 0;
    public static final int ORIENTATION_PORTRAIT = 1;
    public static final int ORIENTATION_LANDSCAPE = 2;

    public static String getNetworkVersion() {
//        try {
//            return MobileAds.getVersionString();
//        } catch (Throwable e) {
//
//        }
        return "";
    }
}
