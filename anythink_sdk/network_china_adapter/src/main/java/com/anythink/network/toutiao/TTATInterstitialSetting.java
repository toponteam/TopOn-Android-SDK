/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.toutiao;

import com.anythink.core.api.ATMediationSetting;

/**
 * Created by zhou on 2018/6/27.
 */
@Deprecated
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
