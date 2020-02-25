package com.anythink.network.flurry;

import com.flurry.android.FlurryAgent;

/**
 * Created by zhou on 2018/7/4.
 */

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
