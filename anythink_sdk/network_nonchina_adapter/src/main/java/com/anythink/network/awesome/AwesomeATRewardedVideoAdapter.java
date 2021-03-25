/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.awesome;

import android.app.Activity;
import android.content.Context;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.Map;

import tv.superawesome.sdk.publisher.SAVideoAd;

public class AwesomeATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    int mPlacementId;
    boolean isReward;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("placement_id")) {
            try {
                mPlacementId = Integer.parseInt(serverExtras.get("placement_id").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mPlacementId == 0) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "placement_id could not be null.");
            }
            return;
        }

        AwesomeATInitManager.getInstance().initSDK(context, serverExtras);

//        // to display test ads
//        SAVideoAd.enableTestMode ();
//
//        // set configuration to production
//        SAVideoAd.setConfigurationProduction ();
//
//        // lock orientation to portrait or landscape
//        SAVideoAd.setOrientationLandscape ();
//
//        // enable or disable the android back button
//        SAVideoAd.enableBackButton ();
//
//        // enable or disable a close button
//        SAVideoAd.enableCloseButton ();
//
//        // enable or disable auto-closing at the end
////        SAVideoAd.disableCloseAtEnd ();
//        SAVideoAd.enableCloseAtEnd ();
//
//        // make the whole video surface area clickable
//        SAVideoAd.enableSmallClickButton ();

        AwesomeATInitManager.getInstance().putLoadResultAdapter(String.valueOf(mPlacementId), this);
        // start loading ad data for a placement
        SAVideoAd.load(mPlacementId, context);
    }


    @Override
    public void show(Activity activity) {
        isReward = false;
        // display the ad
        AwesomeATInitManager.getInstance().putAdapter(String.valueOf(mPlacementId), this);
        SAVideoAd.play(mPlacementId, activity);
    }


    @Override
    public boolean isAdReady() {
        return SAVideoAd.hasAdAvailable(mPlacementId);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public String getNetworkSDKVersion() {
        return AwesomeATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void destory() {

    }


    //--------------------------------------------------------------------------
    public void onRewardedVideoAdDataLoaded() {

    }

    public void onRewardedVideoAdLoaded() {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }
    }

    public void onRewardedVideoAdFailed(String code, String msg) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(code, msg);
        }
    }

    public void onRewardedVideoAdPlayStart() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayStart();
        }
    }

    public void onRewardedVideoAdPlayEnd() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd();
        }
    }

    public void onRewardedVideoAdPlayFailed(String code, String msg) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayFailed(code, msg);
        }
    }

    public void onRewardedVideoAdClosed() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdClosed();
        }
    }

    public void onRewardedVideoAdPlayClicked() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked();
        }
    }

    public void onReward() {
        if (mImpressionListener != null) {
            mImpressionListener.onReward();
        }
    }

    @Override
    public String getNetworkName() {
        return AwesomeATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(mPlacementId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
