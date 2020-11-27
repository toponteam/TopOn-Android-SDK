/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.rewardvideo.bussiness;

import android.content.Context;

import com.anythink.core.common.FormatLoadParams;
import com.anythink.rewardvideo.api.ATRewardVideoListener;

public class RewardedVideoLoadParams extends FormatLoadParams {
    ATRewardVideoListener listener;
    Context context;
    String userId;
    String userData;
}
