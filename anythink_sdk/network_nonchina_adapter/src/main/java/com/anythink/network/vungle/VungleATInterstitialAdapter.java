/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.vungle;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.vungle.warren.AdConfig;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

public class VungleATInterstitialAdapter extends CustomInterstitialAdapter {

    private final String TAG = VungleATInterstitialAdapter.class.getSimpleName();
    String mPlacementId;
    AdConfig mAdConfig;

    private LoadAdCallback loadAdCallback = new LoadAdCallback() {
        @Override
        public void onAdLoad(String s) {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        }

        @Override
        public void onError(String s, VungleException throwable) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", throwable.toString());
            }
        }
    };

    private final PlayAdCallback vungleDefaultListener = new PlayAdCallback() {

        @Override
        public void onAdEnd(String placementReferenceId, boolean wasSuccessFulView, boolean wasCallToActionClicked) {
            // Called when user exits the ad and control is returned to your application
            // if wasSuccessfulView is true, the user watched the ad and could be rewarded
            // if wasCallToActionClicked is true, the user clicked the call to action button in the ad.


        }

        @Override
        public void onAdEnd(String s) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdVideoEnd();
                mImpressListener.onInterstitialAdClose();
            }
        }

        @Override
        public void onAdClick(String s) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked();
            }
        }

        @Override
        public void onAdRewarded(String s) {

        }

        @Override
        public void onAdLeftApplication(String s) {

        }

        @Override
        public void onAdStart(String placementReferenceId) {
            // Called before playing an ad
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdShow();
                mImpressListener.onInterstitialAdVideoStart();
            }
        }

        @Override
        public void onError(String placementReferenceId, VungleException throwable) {
            // Called after playAd(placementId, adConfig) is unable to play the ad
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdVideoError("", throwable.toString());
            }
        }

        @Override
        public void onAdViewed(String s) {

        }
    };


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String mAppId = (String) serverExtras.get("app_id");
        mPlacementId = (String) serverExtras.get("placement_id");


        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mPlacementId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "vungle appid & placementId is empty.");
            }
            return;
        }

        mAdConfig = new AdConfig();
        VungleATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new VungleATInitManager.InitListener() {
            @Override
            public void onSuccess() {
                try {
                    Vungle.loadAd(mPlacementId, loadAdCallback);
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", throwable.getMessage());
                }
            }
        });
    }

    @Override
    public boolean isAdReady() {
        return Vungle.canPlayAd(mPlacementId);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return VungleATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkSDKVersion() {
        return VungleATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void show(Activity activity) {
        if (Vungle.canPlayAd(mPlacementId)) {
            // Play a Placement ad with Placement ID, you can pass AdConfig to customize your ad
            Vungle.playAd(mPlacementId, mAdConfig, vungleDefaultListener);
        }
    }

    @Override
    public void destory() {
        loadAdCallback = null;
        mAdConfig = null;
    }

    @Override
    public String getNetworkName() {
        return VungleATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }


}
