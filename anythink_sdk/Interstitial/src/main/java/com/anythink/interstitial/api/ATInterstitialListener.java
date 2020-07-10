package com.anythink.interstitial.api;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ATAdInfo;


/**
 * Copyright (C) 2018 {XX} Science and Technology Co., Ltd.
 *
 * @version V{2.3.0}
 * @Author ：Created by zhoushubin on 2018/9/19.
 * @Email: zhoushubin@salmonads.com
 */
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
