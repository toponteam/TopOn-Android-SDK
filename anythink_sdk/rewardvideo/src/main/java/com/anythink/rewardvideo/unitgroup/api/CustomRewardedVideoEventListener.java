package com.anythink.rewardvideo.unitgroup.api;

import com.anythink.core.api.AdError;

public interface CustomRewardedVideoEventListener {
    public void onRewardedVideoAdPlayStart(CustomRewardVideoAdapter customRewardVideoAd);

    public void onRewardedVideoAdPlayEnd(CustomRewardVideoAdapter customRewardVideoAd);

    public void onRewardedVideoAdPlayFailed(CustomRewardVideoAdapter customRewardVideoAd, AdError errorCode);

    public void onRewardedVideoAdClosed(CustomRewardVideoAdapter customRewardVideoAd);

    public void onRewardedVideoAdPlayClicked(CustomRewardVideoAdapter customRewardVideoAd);

    public void onReward(CustomRewardVideoAdapter customRewardVideoAdapter);
}
