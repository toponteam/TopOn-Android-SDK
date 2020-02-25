package com.anythink.network.facebook;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class FacebookRewardedVideoSetting implements ATMediationSetting {

    String mRewardData = "";

    @Override
    public int getNetworkType() {
        return FacebookATConst.NETWORK_FIRM_ID;
    }

    public void setRewardData(String rewardData) {
        mRewardData = rewardData;
    }

    public String getRewardData() {
        return mRewardData;
    }

}
