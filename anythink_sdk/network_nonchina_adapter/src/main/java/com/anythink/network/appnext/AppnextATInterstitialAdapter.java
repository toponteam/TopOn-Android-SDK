/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.appnext;

import android.app.Activity;
import android.content.Context;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.appnext.ads.interstitial.Interstitial;
import com.appnext.core.AppnextAdCreativeType;
import com.appnext.core.callbacks.OnAdClicked;
import com.appnext.core.callbacks.OnAdClosed;
import com.appnext.core.callbacks.OnAdError;
import com.appnext.core.callbacks.OnAdLoaded;
import com.appnext.core.callbacks.OnAdOpened;

import java.util.Map;

public class AppnextATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = AppnextATInterstitialAdapter.class.getSimpleName();

    String mPlacementId;

    Interstitial mInterstitial;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {


        if (serverExtras.containsKey("placement_id")) {
            mPlacementId = (String) serverExtras.get("placement_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "placement_id is empty!");
            }
            return;
        }

        AppnextATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras);

        mInterstitial = new Interstitial(context.getApplicationContext(), mPlacementId);

        // Get callback for ad loaded
        mInterstitial.setOnAdLoadedCallback(new OnAdLoaded() {
            @Override
            public void adLoaded(String s, AppnextAdCreativeType appnextAdCreativeType) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }
        });// Get callback for ad opened
        mInterstitial.setOnAdOpenedCallback(new OnAdOpened() {
            @Override
            public void adOpened() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }
        });// Get callback for ad clicked
        mInterstitial.setOnAdClickedCallback(new OnAdClicked() {
            @Override
            public void adClicked() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }
        });// Get callback for ad closed
        mInterstitial.setOnAdClosedCallback(new OnAdClosed() {
            @Override
            public void onAdClosed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }
        });// Get callback for ad error
        mInterstitial.setOnAdErrorCallback(new OnAdError() {
            @Override
            public void adError(String error) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", error);
                }
            }
        });

        mInterstitial.loadAd();

    }

    @Override
    public boolean isAdReady() {
        if (mInterstitial != null) {
            return mInterstitial.isAdLoaded();
        }
        return false;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AppnextATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        if (mInterstitial != null) {
            mInterstitial.showAd();
        }
    }

    @Override
    public void destory() {
        if (mInterstitial != null) {
            mInterstitial.destroy();
            mInterstitial = null;
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return AppnextATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }
}
