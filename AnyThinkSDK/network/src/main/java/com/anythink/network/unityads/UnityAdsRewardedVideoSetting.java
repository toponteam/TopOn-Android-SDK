package com.anythink.network.unityads;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class UnityAdsRewardedVideoSetting implements ATMediationSetting {

    @Override
    public int getNetworkType() {
        return UnityAdsATConst.NETWORK_FIRM_ID;
    }



}
