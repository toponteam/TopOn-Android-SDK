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
