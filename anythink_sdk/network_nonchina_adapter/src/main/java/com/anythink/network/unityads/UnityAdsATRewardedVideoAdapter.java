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

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.metadata.PlayerMetaData;

import java.util.Map;

public class UnityAdsATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private static final String TAG = UnityAdsATRewardedVideoAdapter.class.getSimpleName();

    String placement_id = "";

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String game_id = (String) serverExtras.get("game_id");
        placement_id = (String) serverExtras.get("placement_id");

        if (TextUtils.isEmpty(game_id) || TextUtils.isEmpty(placement_id)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "unityads game_id, placement_id is empty!");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "UnityAds context must be activity.");
            }
            return;
        }

        PlayerMetaData playerMetaData = new PlayerMetaData(context.getApplicationContext());
        playerMetaData.setServerId(mUserId);
        playerMetaData.commit();

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
    public void show(Activity activity) {
        if (activity != null) {
            UnityAdsATInitManager.getInstance().putAdapter(placement_id, this);
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

    @Override
    public void destory() {
    }


    void notifyLoaded(String placementId) {
        if (mLoadListener != null && placement_id.equals(placementId)) {
            mLoadListener.onAdCacheLoaded();
        }
    }

    void notifyLoadFail(String code, String msg) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(code, msg);
        }
    }

    void notifyStart(String placementId) {
        if (mImpressionListener != null && placement_id.equals(placementId)) {
            mImpressionListener.onRewardedVideoAdPlayStart();
        }
    }

    void notifyFinish(String placementId, UnityAds.FinishState finishState) {
        if (mImpressionListener != null && placement_id.equals(placementId)) {
            switch (finishState) {
                case ERROR:
                    mImpressionListener.onRewardedVideoAdPlayFailed("", " play video error");
                    mImpressionListener.onRewardedVideoAdClosed();
                    break;
                case COMPLETED:
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                    mImpressionListener.onReward();
                    mImpressionListener.onRewardedVideoAdClosed();
                    break;
                case SKIPPED:
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                    mImpressionListener.onRewardedVideoAdClosed();
                    break;
                default:
                    break;

            }
        }
    }

    void notifyClick(String placementId) {
        if (mImpressionListener != null && placement_id.equals(placementId)) {
            mImpressionListener.onRewardedVideoAdPlayClicked();
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return UnityAdsATInitManager.getInstance().getNetworkVersion();
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