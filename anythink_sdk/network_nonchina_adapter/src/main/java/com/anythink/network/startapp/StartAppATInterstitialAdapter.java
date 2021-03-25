/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.startapp;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.model.AdPreferences;

import java.util.Map;

public class StartAppATInterstitialAdapter extends CustomInterstitialAdapter {

    StartAppAd startAppAd;
    String adTag = "";

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String appId = "";

        if (serverExtras.containsKey("app_id")) {
            appId = serverExtras.get("app_id").toString();
        }
        if (serverExtras.containsKey("ad_tag")) {
            adTag = serverExtras.get("ad_tag").toString();
        }

        if (adTag == null) {
            adTag = "";
        }


        if (TextUtils.isEmpty(appId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id could not be null.");
            }
            return;
        }

        int isVideo = 0;
        try {
            if (serverExtras.containsKey("is_video")) {
                String is_video = serverExtras.get("is_video").toString();

                if (!TextUtils.isEmpty(is_video)) {
                    isVideo = Integer.parseInt(is_video);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        StartAppATInitManager.getInstance().initSDK(context, serverExtras);
        startAppAd = new StartAppAd(context);

        StartAppAd.AdMode adMode = StartAppAd.AdMode.FULLPAGE;
        if (isVideo == 1) {
            adMode = StartAppAd.AdMode.VIDEO;
        }
        AdPreferences adPreferences = new AdPreferences();
        if (!TextUtils.isEmpty(adTag)) {
            adPreferences.setAdTag(adTag);
        }
        startAppAd.loadAd(adMode, adPreferences, new AdEventListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onFailedToReceiveAd(Ad ad) {
                if (mLoadListener != null) {
                    if (ad != null) {
                        mLoadListener.onAdLoadError("", ad.getErrorMessage());
                    } else {
                        mLoadListener.onAdLoadError("", "StartApp has not error msg.");
                    }
                }
            }
        });

    }

    @Override
    public void show(Activity activity) {
        if (startAppAd != null && startAppAd.isReady()) {
            AdDisplayListener adDisplayListener = new AdDisplayListener() {
                @Override
                public void adHidden(Ad ad) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClose();
                    }
                }

                @Override
                public void adDisplayed(Ad ad) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdShow();
                    }
                }

                @Override
                public void adClicked(Ad ad) {
                    if (mImpressListener != null) {
                        mImpressListener.onInterstitialAdClicked();
                    }
                }

                @Override
                public void adNotDisplayed(Ad ad) {
                }
            };

            if (!TextUtils.isEmpty(adTag)) {
                startAppAd.showAd(adTag, adDisplayListener);
            } else {
                startAppAd.showAd(adDisplayListener);
            }
        }
    }


    @Override
    public boolean isAdReady() {
        return startAppAd != null && startAppAd.isReady();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public String getNetworkSDKVersion() {
        return StartAppATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void destory() {
        if (startAppAd != null) {
            startAppAd.setVideoListener(null);
            startAppAd = null;
        }
    }

    @Override
    public String getNetworkName() {
        return StartAppATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return adTag;
    }
}
