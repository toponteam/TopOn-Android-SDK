package com.anythink.network.inmobi;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

@Deprecated
public class InmobiRewardedVideoSetting implements ATMediationSetting {

    @Override
    public int getNetworkType() {
        return InmobiATConst.NETWORK_FIRM_ID;
    }

}
