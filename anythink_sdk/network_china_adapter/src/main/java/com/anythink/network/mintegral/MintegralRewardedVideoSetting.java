/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

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
