package com.anythink.network.ironsource;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class IronsourceRewardedVideoSetting implements ATMediationSetting {

    @Override
    public int getNetworkType() {
        return IronsourceATConst.NETWORK_FIRM_ID;
    }

}
