package com.anythink.interstitial.unitgroup.api;

import com.anythink.core.api.AdError;

public interface CustomInterstitialEventListener {
    public void onInterstitialAdClicked(CustomInterstitialAdapter adapter);
    public void onInterstitialAdShow(CustomInterstitialAdapter adapter);
    public void onInterstitialAdClose(CustomInterstitialAdapter adapter);
    public void onInterstitialAdVideoStart(CustomInterstitialAdapter adapter);
    public void onInterstitialAdVideoEnd(CustomInterstitialAdapter adapter);
    public void onInterstitialAdVideoError(CustomInterstitialAdapter adapter, AdError adError);
}
