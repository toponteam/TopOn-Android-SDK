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

    public void onInterstitialAdClicked(ATAdInfo entity);

    public void onInterstitialAdShow(ATAdInfo entity);

    public void onInterstitialAdClose(ATAdInfo entity);

    public void onInterstitialAdVideoStart();


    public void onInterstitialAdVideoEnd();

    public void onInterstitialAdVideoError(AdError adError);

}
