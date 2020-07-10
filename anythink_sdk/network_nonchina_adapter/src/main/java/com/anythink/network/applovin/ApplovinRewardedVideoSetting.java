package com.anythink.network.applovin;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class ApplovinRewardedVideoSetting implements ATMediationSetting {

    @Override
    public int getNetworkType() {
        return ApplovinATConst.NETWORK_FIRM_ID;
    }

}
