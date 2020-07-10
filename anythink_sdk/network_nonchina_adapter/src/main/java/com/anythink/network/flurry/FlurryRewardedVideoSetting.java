package com.anythink.network.flurry;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class FlurryRewardedVideoSetting implements ATMediationSetting {

    @Override
    public int getNetworkType() {
        return FlurryATConst.NETWORK_FIRM_ID;
    }


}
