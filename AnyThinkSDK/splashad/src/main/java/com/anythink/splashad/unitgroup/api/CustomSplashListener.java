package com.anythink.splashad.unitgroup.api;

import com.anythink.core.api.AdError;

public interface CustomSplashListener {
    public void onSplashAdLoaded(CustomSplashAdapter customSplashAd); //Ad Request Success

    public void onSplashAdFailed(CustomSplashAdapter customSplashAd, AdError adError);//Ad Request Fail

    public void onSplashAdShow(CustomSplashAdapter customSplashAd);//Ad show

    public void onSplashAdClicked(CustomSplashAdapter customSplashAd); //Ad Click

    public void onSplashAdDismiss(CustomSplashAdapter customSplashAd); //Ad Dismiss
}
