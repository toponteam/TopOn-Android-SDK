/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.flurry;

import com.flurry.android.FlurryAgent;


public class FlurryATConst {
    /***
     * GDRP location map key
     */
    public static final  String LOCATION_MAP_KEY_GDPR_IABSTR = "FlurryGdprIabstr";
    public static final  String LOCATION_MAP_KEY_GDPR_isGdprScope = "FlurryGdprIsGdprScope";

    public static final int NETWORK_FIRM_ID = 4;

    public static String getNetworkVersion() {
        try {
            return FlurryAgent.getReleaseVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
