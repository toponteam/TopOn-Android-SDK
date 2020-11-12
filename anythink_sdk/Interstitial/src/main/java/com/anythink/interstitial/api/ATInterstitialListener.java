package com.anythink.interstitial.api;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ATAdInfo;



public interface ATInterstitialListener {
    public void onInterstitialAdLoaded();

    public void onInterstitialAdLoadFail(AdError adError);

    public void onInterstitialAdClicked(ATAdInfo adInfo);

    public void onInterstitialAdShow(ATAdInfo adInfo);

    public void onInterstitialAdClose(ATAdInfo adInfo);

    public void onInterstitialAdVideoStart(ATAdInfo adInfo);


    public void onInterstitialAdVideoEnd(ATAdInfo adInfo);

    public void onInterstitialAdVideoError(AdError adError);

}
