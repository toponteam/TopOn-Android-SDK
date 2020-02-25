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
