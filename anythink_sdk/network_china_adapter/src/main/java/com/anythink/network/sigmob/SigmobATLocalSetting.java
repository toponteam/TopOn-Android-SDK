/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.sigmob;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */
@Deprecated
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
