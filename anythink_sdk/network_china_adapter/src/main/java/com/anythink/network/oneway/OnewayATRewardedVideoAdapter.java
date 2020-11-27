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

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.Map;

import mobi.oneway.export.Ad.OWRewardedAd;
import mobi.oneway.export.AdListener.OWRewardedAdListener;
import mobi.oneway.export.enums.OnewayAdCloseType;
import mobi.oneway.export.enums.OnewaySdkError;


public class OnewayATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private final String TAG = getClass().getSimpleName();

    String mPublishId;
    String mSlotId;
    private OWRewardedAd owRewardedAd;

    OWRewardedAdListener mListener = new OWRewardedAdListener() {
        @Override
        public void onAdReady() {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        }

        @Override
        public void onAdShow(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayStart();
            }
        }

        @Override
        public void onAdClick(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayClicked();
            }
        }

        @Override
        public void onAdClose(String s, OnewayAdCloseType onewayAdCloseType) {

            if (mImpressionListener != null) {
                if (onewayAdCloseType == OnewayAdCloseType.COMPLETED) {
                    mImpressionListener.onReward();
                }
                mImpressionListener.onRewardedVideoAdClosed();
            }
        }

        @Override
        public void onAdFinish(String s, OnewayAdCloseType onewayAdCloseType, String s1) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayEnd();
            }
        }

        @Override
        public void onSdkError(OnewaySdkError error, String s) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError(error.name(), s);
            }
        }
    };

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        if (serverExtra.containsKey("publisher_id") && serverExtra.containsKey("slot_id")) {
            mPublishId = serverExtra.get("publisher_id").toString();
            mSlotId = serverExtra.get("slot_id").toString();
            return true;
        }
        return false;
    }

    @Override
    public boolean isAdReady() {
        return (owRewardedAd != null && owRewardedAd.isReady());
    }

    @Override
    public void show(Activity activity) {
        try {
            if (activity != null) {
                if (isAdReady()) {
                    owRewardedAd.setListener(mListener);
                    owRewardedAd.show(activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        if (serverExtra.containsKey("publisher_id")) {
            publishId = serverExtra.get("publisher_id").toString();
        }
        if (serverExtra.containsKey("slot_id")) {
            slotId = serverExtra.get("slot_id").toString();
        }

        if (TextUtils.isEmpty(publishId) || TextUtils.isEmpty(slotId)) {
            Log.e(TAG, "publishId or placementId is empty, place check once more....");
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
        OnewayATInitManager.getInstance().initSDK(context, serverExtra);
        owRewardedAd = new OWRewardedAd(((Activity) context), mSlotId, mListener);
        if (owRewardedAd.isReady()) {
            Log.i(TAG, "loadRewardVideoAd: ready...");
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        } else {
            owRewardedAd.loadAd();
        }
    }

    @Override
    public void destory() {
        if (owRewardedAd != null) {
            owRewardedAd.setListener(null);
            owRewardedAd.destory();
            owRewardedAd = null;
        }
        mListener = null;
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
