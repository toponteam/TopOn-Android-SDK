/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.oneway;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;

import java.util.Map;

import mobi.oneway.export.Ad.OWInterstitialAd;
import mobi.oneway.export.Ad.OWInterstitialImageAd;
import mobi.oneway.export.AdListener.OWInterstitialAdListener;
import mobi.oneway.export.AdListener.OWInterstitialImageAdListener;
import mobi.oneway.export.enums.OnewayAdCloseType;
import mobi.oneway.export.enums.OnewaySdkError;



public class OnewayATInterstitialAdapter extends CustomInterstitialAdapter {
    public static final String TAG = OnewayATInterstitialAdapter.class.getSimpleName();


    String mPublishId;
    String mSlotId;
    String mIsVideo;
    private OWInterstitialAd owInterstitialAd;
    private OWInterstitialImageAd owInterstitialImageAd;

    OWInterstitialAdListener mListener = new OWInterstitialAdListener() {
        @Override
        public void onAdReady() {
            // The ad is ready, you can call the show () method to play the ad
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        }

        @Override
        public void onAdShow(String tag) {
            // Advertising has begun
            if (mImpressListener != null) {

                mImpressListener.onInterstitialAdShow();
                mImpressListener.onInterstitialAdVideoStart();
            }
        }

        @Override
        public void onAdClick(String tag) {
            // Ad click event
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked();
            }
        }

        @Override
        public void onAdClose(String tag, OnewayAdCloseType type) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClose();
            }
        }

        @Override
        public void onAdFinish(String s, OnewayAdCloseType onewayAdCloseType, String s1) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdVideoEnd();
            }
        }


        @Override
        public void onSdkError(OnewaySdkError error, String message) {
            // An error occurred during SDK initialization or advertisement playback. You can save the error log here to troubleshoot the cause of the error.
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError(error.name(), message);
            }
        }
    };

    OWInterstitialImageAdListener mInterstitialImageListener = new OWInterstitialImageAdListener() {
        @Override
        public void onAdReady() {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        }

        @Override
        public void onAdShow(String s) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdShow();
            }
        }

        @Override
        public void onAdClick(String s) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked();
            }
        }

        @Override
        public void onAdClose(String s, OnewayAdCloseType onewayAdCloseType) {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClose();
            }
        }

        @Override
        public void onAdFinish(String s, OnewayAdCloseType onewayAdCloseType, String s1) {

        }

        @Override
        public void onSdkError(OnewaySdkError error, String message) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError(String.valueOf(error.name()), message);
            }
        }
    };

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        if (serverExtra.containsKey("publisher_id") && serverExtra.containsKey("slot_id")) {
            mPublishId = serverExtra.get("publisher_id").toString();
            mSlotId = serverExtra.get("slot_id").toString();
            mIsVideo = serverExtra.get("is_video").toString();
            return true;
        }
        return false;
    }

    @Override
    public boolean isAdReady() {
        if (TextUtils.equals(mIsVideo, "1")) {
            return owInterstitialAd != null && owInterstitialAd.isReady();
        } else {
            return owInterstitialImageAd != null && owInterstitialImageAd.isReady();
        }
    }

    @Override
    public void show(Activity activity) {
        if (activity != null) {
            if (isAdReady()) {
                if (TextUtils.equals(mIsVideo, "1")) {
                    owInterstitialAd.setListener(mListener);
                    owInterstitialAd.show(activity);
                } else {
                    owInterstitialImageAd.setListener(mInterstitialImageListener);
                    owInterstitialImageAd.show(activity);
                }
            }
        }
    }

    @Override
    public String getNetworkName() {
        return OnewayATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String publishId = "";
        String slotId = "";
        String isVideo = "0";

        if (serverExtra.containsKey("publisher_id")) {
            publishId = serverExtra.get("publisher_id").toString();
        }
        if (serverExtra.containsKey("slot_id")) {
            slotId = serverExtra.get("slot_id").toString();
        }
        if (serverExtra.containsKey("is_video")) {
            isVideo = serverExtra.get("is_video").toString();
        }

        if (TextUtils.isEmpty(publishId) || TextUtils.isEmpty(slotId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " publishId or slotId is empty.");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Oneway context must be activity.");
            }
            return;
        }

        mPublishId = publishId;
        mSlotId = slotId;
        mIsVideo = isVideo;
        OnewayATInitManager.getInstance().initSDK(context, serverExtra);

        if (TextUtils.equals(mIsVideo, "1")) {//video
            owInterstitialAd = new OWInterstitialAd(((Activity) context), mSlotId, mListener);
            if (owInterstitialAd.isReady()) {
                Log.i(TAG, "intersitital video : ready...");
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            } else {
                owInterstitialAd.loadAd();
            }

        } else {//image
            owInterstitialImageAd = new OWInterstitialImageAd(((Activity) context), mSlotId, mInterstitialImageListener);
            if (owInterstitialImageAd.isReady()) {
                Log.i(TAG, "intersitital image : ready...");
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            } else {
                owInterstitialImageAd.loadAd();
            }
        }
    }

    @Override
    public void destory() {
        if (owInterstitialAd != null) {
            owInterstitialAd.setListener(null);
            owInterstitialAd.destory();
            owInterstitialAd = null;
        }

        if (owInterstitialImageAd != null) {
            owInterstitialImageAd.setListener(null);
            owInterstitialImageAd.destory();
            owInterstitialImageAd = null;
        }

        mListener = null;
        mInterstitialImageListener = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mSlotId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return OnewayATConst.getNetworkVersion();
    }
}
