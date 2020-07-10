package com.anythink.network.tapjoy;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class TapjoyRewardedVideoSetting implements ATMediationSetting {

    @Override
    public int getNetworkType() {
        return TapjoyATConst.NETWORK_FIRM_ID;
    }

    private String gcmSender;

    public String getGcmSender() {
        return gcmSender;
    }

    public void setGcmSender(String pGcmSender) {
        gcmSender = pGcmSender;
    }
}
