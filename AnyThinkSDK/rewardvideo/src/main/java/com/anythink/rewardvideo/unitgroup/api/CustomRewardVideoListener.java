package com.anythink.rewardvideo.unitgroup.api;

import com.anythink.core.api.AdError;

public interface CustomRewardVideoListener {
    public void onRewardedVideoAdDataLoaded(CustomRewardVideoAdapter customRewardVideoAd); //Ad Data download success

    public void onRewardedVideoAdLoaded(CustomRewardVideoAdapter customRewardVideoAd); //Ad Data and Resource download success

    public void onRewardedVideoAdFailed(CustomRewardVideoAdapter customRewardVideoAd, AdError adError);//Ad Request Fail

}
