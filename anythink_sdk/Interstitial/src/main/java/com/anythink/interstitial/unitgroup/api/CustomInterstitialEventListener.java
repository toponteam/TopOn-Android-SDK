package com.anythink.interstitial.unitgroup.api;


public interface CustomInterstitialEventListener {
    void onInterstitialAdClicked();
    void onInterstitialAdShow();
    void onInterstitialAdClose();
    void onInterstitialAdVideoStart();
    void onInterstitialAdVideoEnd();
    void onInterstitialAdVideoError(String errorCode, String errorMsg);
}
