/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.admob;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import java.util.Map;

/**
 * Interstitial Adapter for Google Ad Manager
 */

public class GoogleAdATInterstitialAdapter extends CustomInterstitialAdapter {

    PublisherInterstitialAd mInterstitialAd;
    private String unitid = "";

    boolean isAdReady = false;

    /***
     * load ad
     */
    private void startLoadAd(Context context) {

        mInterstitialAd = new PublisherInterstitialAd(context.getApplicationContext());
        mInterstitialAd.setAdUnitId(unitid);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                isAdReady = true;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(errorCode + "", "");
                }
            }

            @Override
            public void onAdOpened() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onAdLeftApplication() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }
        });


        mInterstitialAd.loadAd(new PublisherAdRequest.Builder().build());

    }

    @Override
    public void destory() {
        try {
            if (mInterstitialAd != null) {
                mInterstitialAd.setAdListener(null);
                mInterstitialAd = null;
            }
        } catch (Exception e) {
        }
    }


    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {

        unitid = (String) serverExtras.get("unit_id");

        if (TextUtils.isEmpty(unitid)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "unitid is empty.");
            }
            return;
        }


        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    startLoadAd(context);
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }
        });


    }

    @Override
    public boolean isAdReady() {
        try {
            if (check()) {
                return mInterstitialAd.isLoaded();
            }
        } catch (Throwable e) {

        }
        return isAdReady;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AdMobATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    /***
     * Show Ad
     */
    @Override
    public void show(Activity activity) {
        if (check()) {
            isAdReady = false;
            mInterstitialAd.show();
        }
    }

    private boolean check() {
        if (mInterstitialAd == null) {
            return false;
        }
        return true;
    }

    @Override
    public String getNetworkSDKVersion() {
        return AdMobATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getGoogleAdManagerName();
    }

    @Override
    public String getNetworkPlacementId() {
        return unitid;
    }
}