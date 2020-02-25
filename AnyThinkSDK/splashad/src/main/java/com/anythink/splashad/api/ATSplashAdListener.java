package com.anythink.splashad.api;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ATAdInfo;

public interface ATSplashAdListener {
    public void onAdLoaded();

    public void onNoAdError(AdError adError);

    public void onAdShow(ATAdInfo entity);

    public void onAdClick(ATAdInfo entity);

    public void onAdDismiss(ATAdInfo entity);

    public void onAdTick(long millisUtilFinished);
}
