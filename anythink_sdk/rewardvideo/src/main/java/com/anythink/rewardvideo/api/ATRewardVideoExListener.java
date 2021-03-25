/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.rewardvideo.api;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;

/**
 * RewardedVideo event callback
 */
public interface ATRewardVideoExListener extends ATRewardVideoListener {

    void onDeeplinkCallback(ATAdInfo adInfo, boolean isSuccess);

}
