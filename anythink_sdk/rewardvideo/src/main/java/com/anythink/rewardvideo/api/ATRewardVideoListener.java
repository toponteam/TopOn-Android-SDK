/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.rewardvideo.api;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ATAdInfo;

/**
 * RewardedVideo event callback
 */
public interface ATRewardVideoListener {

    public void onRewardedVideoAdLoaded();

    public void onRewardedVideoAdFailed(AdError errorCode);

    public void onRewardedVideoAdPlayStart(ATAdInfo adInfo);

    public void onRewardedVideoAdPlayEnd(ATAdInfo adInfo);

    public void onRewardedVideoAdPlayFailed(AdError errorCode, ATAdInfo adInfo);

    public void onRewardedVideoAdClosed(ATAdInfo adInfo);

    public void onRewardedVideoAdPlayClicked(ATAdInfo adInfo);

    public void onReward(ATAdInfo adInfo);

}
