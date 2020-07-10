package com.anythink.nativead.splash.api;

import com.anythink.core.api.ATAdInfo;

public interface ATNativeSplashListener {
    public void onAdLoaded();

    public void onNoAdError(String msg);

    public void onAdShow(ATAdInfo entity);

    public void onAdClick(ATAdInfo entity);

    public void onAdSkip();

    public void onAdTimeOver();

    public void onAdTick(long millisUtilFinished);
}
