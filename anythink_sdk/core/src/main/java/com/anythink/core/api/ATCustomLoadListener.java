package com.anythink.core.api;

public interface ATCustomLoadListener {
    public void onAdDataLoaded();

    public void onAdCacheLoaded(BaseAd... baseAds);

    public void onAdLoadError(String errorCode, String errorMsg);
}
