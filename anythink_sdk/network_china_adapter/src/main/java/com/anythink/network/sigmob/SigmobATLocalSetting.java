package com.anythink.network.sigmob;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class SigmobATLocalSetting implements ATMediationSetting {

    private boolean isUseRewardedVideoAsInterstitial = false;

    public void setUseRewardedVideoAsInterstitial(boolean isUse) {
        isUseRewardedVideoAsInterstitial = isUse;
    }

    public boolean isUseRewardedVideoAsInterstitial() {
        return isUseRewardedVideoAsInterstitial;
    }

    @Override
    public int getNetworkType() {
        return SigmobATConst.NETWORK_FIRM_ID;
    }

}
