package com.anythink.network.chartboost;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class ChartboostRewardedVideoSetting implements ATMediationSetting {

    @Override
    public int getNetworkType() {
        return ChartboostATConst.NETWORK_FIRM_ID;
    }

}
