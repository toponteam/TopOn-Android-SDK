/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.interstitial.unitgroup.api;


public interface CustomInterstitialEventListener {
    void onInterstitialAdClicked();
    void onInterstitialAdShow();
    void onInterstitialAdClose();
    void onInterstitialAdVideoStart();
    void onInterstitialAdVideoEnd();
    void onInterstitialAdVideoError(String errorCode, String errorMsg);
}
