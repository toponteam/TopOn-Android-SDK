package com.anythink.network.sigmob;

import com.sigmob.windad.WindAds;

public class SigmobATConst {

    public static final String IS_USE_REWARDED_VIDEO_AS_INTERSTITIAL = "is_use_rewarded_video_as_interstitial";
    public static final int NETWORK_FIRM_ID = 29;

    public static String getSDKVersion() {
        try {
            return WindAds.getVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }
}
