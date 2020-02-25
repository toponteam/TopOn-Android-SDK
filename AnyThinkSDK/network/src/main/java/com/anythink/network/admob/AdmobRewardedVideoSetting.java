package com.anythink.network.admob;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class AdmobRewardedVideoSetting implements ATMediationSetting {

    @Override
    public int getNetworkType() {
        return AdmobATConst.NETWORK_FIRM_ID;
    }

}
