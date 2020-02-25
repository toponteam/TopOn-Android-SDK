package com.anythink.network.toutiao;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */

public class TTATInterstitialSetting implements ATMediationSetting {
    int interstitialWidth;

    public void setInterstitialWidth(int width) {
        this.interstitialWidth = width;
    }

    public int getInterstitialWidth() {
        return interstitialWidth;
    }

    @Override
    public int getNetworkType() {
        return TTATConst.NETWORK_FIRM_ID;
    }

}
