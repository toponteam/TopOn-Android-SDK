/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.uniplay;

import android.app.Activity;
import android.content.Context;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.uniplay.adsdk.InterstitialAd;
import com.uniplay.adsdk.InterstitialAdListener;

import java.util.Map;

public class UniplayATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = getClass().getSimpleName();

    String appId = "";
    private InterstitialAd mInterstitialAd;

    // Ad load listener
    InterstitialAdListener mAdListener = new InterstitialAdListener() {

        @Override
        public void onInterstitialAdReady() {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        }

        @Override
        public void onInterstitialAdShow() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdShow();
            }
        }

        @Override
        public void onInterstitialAdClick() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked();
            }
        }

        @Override
        public void onInterstitialAdFailed(String s) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", s);
            }
        }

        @Override
        public void onInterstitialAdClose() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClose();
            }
        }
    };

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        if (serverExtra.containsKey("app_id")) {
            appId = (String) serverExtra.get("app_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id is empty!");
            }
            return;
        }

        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mInterstitialAd = new InterstitialAd(context.getApplicationContext(), appId);
                    mInterstitialAd.setInterstitialAdListener(mAdListener);
                    mInterstitialAd.loadInterstitialAd();
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
        if (mInterstitialAd != null) {
            return mInterstitialAd.isInterstitialAdReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        try {
            if (mInterstitialAd != null && activity != null) {
                mInterstitialAd.showInterstitialAd(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNetworkName() {
        return UniplayATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setInterstitialAdListener(null);
            mInterstitialAd = null;
        }
        mAdListener = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return appId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }
}
