package com.anythink.network.mintegral;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class MintegralRewardedVideoSetting implements ATMediationSetting {

    @Override
    public int getNetworkType() {
        return MintegralATConst.NETWORK_FIRM_ID;
    }

}
