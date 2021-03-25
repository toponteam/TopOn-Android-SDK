/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ksyun;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.network.ks.KSATInitManager;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.ksc.ad.sdk.IKsyunAdInitResultListener;
import com.ksc.ad.sdk.IKsyunAdListener;
import com.ksc.ad.sdk.IKsyunAdLoadListener;
import com.ksc.ad.sdk.IKsyunRewardVideoAdListener;
import com.ksc.ad.sdk.KsyunAdSdk;

import java.util.Map;

public class KsyunATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private final String TAG = getClass().getSimpleName();
    String mSlotid;

    /**
     * Ad load event callback
     */
    IKsyunAdLoadListener mAdLoadListener = new IKsyunAdLoadListener() {
        @Override
        public void onAdInfoSuccess() {
        }

        @Override
        public void onAdInfoFailed(int i, String s) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError(String.valueOf(i), s);
            }
        }

        @Override
        public void onAdLoaded(String s) {
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }

        }
    };

    /**
     * Advertising behavior event callback
     */
    IKsyunAdListener mAdListener = new IKsyunAdListener() {
        @Override
        public void onShowSuccess(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayStart();
            }
        }

        @Override
        public void onShowFailed(String s, int i, String s1) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayFailed(String.valueOf(i), s1);
            }
        }

        @Override
        public void onADComplete(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayEnd();
            }
        }

        @Override
        public void onADClick(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayClicked();
            }
        }

        @Override
        public void onADClose(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdClosed();
            }
        }
    };

    /**
     * Advertising reward event callback
     */
    IKsyunRewardVideoAdListener mRewardListener = new IKsyunRewardVideoAdListener() {
        @Override
        public void onAdAwardSuccess(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onReward();
            }
        }

        @Override
        public void onAdAwardFailed(String s, int i, String s1) {
        }
    };

    @Override
    public boolean isAdReady() {
        return KsyunAdSdk.getInstance().hasAd(mSlotid);
    }

    @Override
    public void show(Activity activity) {
        try {
            if (activity != null) {
                KsyunAdSdk.getInstance().showAd(activity, mSlotid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNetworkName() {
        return KsyunATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String mMediaId = "";
        if (serverExtra.containsKey("media_id")) {
            mMediaId = serverExtra.get("media_id").toString();
        }

        if (serverExtra.containsKey("slot_id")) {
            mSlotid = serverExtra.get("slot_id").toString();
        }

        if (TextUtils.isEmpty(mMediaId) || TextUtils.isEmpty(mSlotid)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "ksyun mediaid or slotid  is empty.");

            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "ksyun context must be activity.");
            }
            return;
        }

        KsyunATInitManager.getInstance().initSDK(((Activity) context), serverExtra, new IKsyunAdInitResultListener() {
            @Override
            public void onSuccess(Map<String, String> map) {
                KsyunAdSdk.getInstance().setAdListener(mAdListener);
                KsyunAdSdk.getInstance().setRewardVideoAdListener(mRewardListener);
                KsyunAdSdk.getInstance().loadAd(mSlotid, mAdLoadListener);
            }

            @Override
            public void onFailure(int i, String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(i + "", s);
                }
            }
        });
    }

    @Override
    public void destory() {
        KsyunAdSdk.getInstance().clearCache();

        mAdLoadListener = null;
        mAdListener = null;
        mRewardListener = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mSlotid;
    }

    @Override
    public String getNetworkSDKVersion() {
        return KsyunATInitManager.getInstance().getNetworkVersion();
    }

}
