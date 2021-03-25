/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.unityads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.unity3d.ads.UnityAds;

import java.util.Map;

public class UnityAdsATInterstitialAdapter extends CustomInterstitialAdapter {
    private static final String TAG = UnityAdsATInterstitialAdapter.class.getSimpleName();

    String placement_id = "";

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "UnityAds context must be activity.");
            }
            return;
        }


        String game_id = (String) serverExtras.get("game_id");
        placement_id = (String) serverExtras.get("placement_id");

        if (TextUtils.isEmpty(game_id) || TextUtils.isEmpty(placement_id)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "unityads game_id, placement_id is empty!");
            }
            return;
        }

        UnityAds.PlacementState placementState = UnityAds.getPlacementState(placement_id);
        if (UnityAds.PlacementState.READY == placementState) {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        } else {
            UnityAdsATInitManager.getInstance().putLoadResultAdapter(placement_id, this);
            UnityAdsATInitManager.getInstance().initSDK(context, serverExtras, new UnityAdsATInitManager.InitListener() {
                @Override
                public void onSuccess() {
                    UnityAds.load(placement_id);
                }

                @Override
                public void onError(String error, String msg) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(error, msg);
                    }
                }
            });

        }
    }

    @Override
    public boolean isAdReady() {
        return UnityAds.isReady(placement_id);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return UnityAdsATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkSDKVersion() {
        return UnityAdsATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void show(Activity activity) {
        UnityAdsATInitManager.getInstance().putAdapter(placement_id, this);
        if (activity != null) {
            UnityAds.show((activity), placement_id);
        }
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        if (serverExtras != null) {
            if (serverExtras.containsKey("game_id") && serverExtras.containsKey("placement_id")) {
                placement_id = (String) serverExtras.get("placement_id");
                return true;
            }
        }
        return false;
    }

    void notifyLoaded(String placementId) {
        if (placementId.equals(placement_id)) {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        }
    }

    void notifyLoadFail(String code, String msg) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(code, msg);
        }
    }

    void notifyStart(String placementId) {
        if (mImpressListener != null && placement_id.equals(placementId)) {
            mImpressListener.onInterstitialAdShow();
        }
    }

    void notifyFinish(String placementId, UnityAds.FinishState finishState) {
        if (mImpressListener != null && placement_id.equals(placementId)) {
            mImpressListener.onInterstitialAdClose();
        }
    }

    void notifyClick(String placementId) {
        if (mImpressListener != null && placement_id.equals(placementId)) {
            mImpressListener.onInterstitialAdClicked();
        }
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return UnityAdsATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return placement_id;
    }

}
