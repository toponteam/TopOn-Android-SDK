/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.rewardvideo.unitgroup.api;


public interface CustomRewardedVideoEventListener {
    void onRewardedVideoAdPlayStart();

    void onRewardedVideoAdPlayEnd();

    void onRewardedVideoAdPlayFailed(String errorCode, String errorMsg);

    void onRewardedVideoAdClosed();

    void onRewardedVideoAdPlayClicked();

    void onReward();

    void onDeeplinkCallback(boolean isSuccess);
}
