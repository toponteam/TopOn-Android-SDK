package com.anythink.rewardvideo.unitgroup.api;


public interface CustomRewardedVideoEventListener {
    void onRewardedVideoAdPlayStart();

    void onRewardedVideoAdPlayEnd();

    void onRewardedVideoAdPlayFailed(String errorCode, String errorMsg);

    void onRewardedVideoAdClosed();

    void onRewardedVideoAdPlayClicked();

    void onReward();
}
