package com.anythink.interstitial.unitgroup.api;

import com.anythink.core.api.AdError;

public interface CustomInterstitialListener {
    public void onInterstitialAdDataLoaded(CustomInterstitialAdapter adapter);
    public void onInterstitialAdLoaded(CustomInterstitialAdapter adapter);
    public void onInterstitialAdLoadFail(CustomInterstitialAdapter adapter, AdError adError);
}
