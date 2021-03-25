/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.interstitial.api;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATAdStatusInfo;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;
import com.anythink.interstitial.business.AdLoadManager;

import java.lang.ref.WeakReference;
import java.util.Map;


public class ATInterstitial {
    public static final String TAG = ATInterstitial.class.getSimpleName();
    public String mPlacementId;
    public Context mContext;
    WeakReference<Activity> mActivityWef;
    public ATInterstitialListener mInterstitialListener;


    AdLoadManager mAdLoadManager;


    private ATInterstitialExListener mInterListener = new ATInterstitialExListener() {
        @Override
        public void onDeeplinkCallback(final ATAdInfo adInfo, final boolean isSuccess) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null && mInterstitialListener instanceof ATInterstitialExListener) {
                        ((ATInterstitialExListener) mInterstitialListener).onDeeplinkCallback(adInfo, isSuccess);
                    }
                }
            });
        }

        @Override
        public void onInterstitialAdLoaded() {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onInterstitialAdLoaded();
                    }
                }
            });

        }

        @Override
        public void onInterstitialAdLoadFail(final AdError errorCode) {
            if (mAdLoadManager != null) {
                mAdLoadManager.setLoadFail(errorCode);
            }
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onInterstitialAdLoadFail(errorCode);
                    }
                }
            });

        }

        @Override
        public void onInterstitialAdVideoStart(final ATAdInfo adInfo) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onInterstitialAdVideoStart(adInfo);
                    }
                }
            });

        }

        @Override
        public void onInterstitialAdVideoEnd(final ATAdInfo adInfo) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onInterstitialAdVideoEnd(adInfo);
                    }
                }
            });

        }

        @Override
        public void onInterstitialAdVideoError(final AdError errorCode) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onInterstitialAdVideoError(errorCode);
                    }
                }
            });

        }

        @Override
        public void onInterstitialAdClose(final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onInterstitialAdClose(entity);
                    }
                }
            });

            //判断是否需要自动刷新
            if (isNeedAutoLoadAfterClose()) {
                load(getRequestContext(), true);
            }
        }

        @Override
        public void onInterstitialAdClicked(final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onInterstitialAdClicked(entity);
                    }
                }
            });

        }

        @Override
        public void onInterstitialAdShow(final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mInterstitialListener != null) {
                        mInterstitialListener.onInterstitialAdShow(entity);
                    }
                }
            });
        }
    };

    public ATInterstitial(Context context, String placementId) {
        mContext = context.getApplicationContext();
        if (context instanceof Activity) {
            mActivityWef = new WeakReference<Activity>((Activity) context);
        }
        mPlacementId = placementId;
        mAdLoadManager = AdLoadManager.getInstance(context, placementId);

    }

    private Context getRequestContext() {
        Activity activity = null;
        if (mActivityWef != null) {
            activity = mActivityWef.get();
        }
        return activity != null ? activity : mContext;
    }

    public void setLocalExtra(Map<String, Object> map) {
        PlacementAdManager.getInstance().putPlacementLocalSettingMap(mPlacementId, map);
    }

    public void load() {
        load(getRequestContext(), false);
    }

    public void load(Context context) {
        load(context != null ? context : getRequestContext(), false);
    }

    private void load(Context context, final boolean isAutoRefresh) {
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_INTERSTITIAL, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");
        mAdLoadManager.startLoadAd(context, isAutoRefresh, mInterListener);
    }

    private boolean isNeedAutoLoadAfterClose() {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(mPlacementId);
        if (placeStrategy != null) {
            return placeStrategy.getAutoRefresh() == 1 && !mAdLoadManager.isLoading();
        }
        return false;
    }


    public void setAdListener(ATInterstitialListener listener) {
        mInterstitialListener = listener;
    }


    public boolean isAdReady() {
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            Log.e(TAG, "SDK init error!");
            return false;
        }

//        if (UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext())
//                .getUploadDataLevel() == ATSDK.FORBIDDEN) {
//            AdError adError = ErrorCode.getErrorCode(ErrorCode.dataLevelLowError, "", "");
//            Log.e(TAG, adError.getDesc());
//            return false; //如果是FORBIDDEN则不去播放
//        }

        boolean isAdReady = mAdLoadManager.isAdReady(mContext);
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_INTERSTITIAL, Const.LOGKEY.API_ISREADY, String.valueOf(isAdReady), "");
        return isAdReady;
    }

    public ATAdStatusInfo checkAdStatus() {
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            Log.e(TAG, "SDK init error!");
            return new ATAdStatusInfo(false, false, null);
        }

        ATAdStatusInfo adStatusInfo = mAdLoadManager.checkAdStatus(mContext);
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_INTERSTITIAL, Const.LOGKEY.API_AD_STATUS, adStatusInfo.toString(), "");

        return adStatusInfo;
    }

    public void show(Activity activity, String scenario) {
        String realScenario = scenario;
        if (CommonSDKUtil.isVailScenario(scenario)) {
            realScenario = scenario;
        }
        controlShow(activity, realScenario);
    }

    public void show(Activity activity) {
        controlShow(activity, "");
    }

    private void controlShow(Activity activity, String scenario) {

        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_INTERSTITIAL, Const.LOGKEY.API_SHOW, Const.LOGKEY.START, "");
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            Log.e(TAG, "Show error: SDK init error!");
//            AdError adError = ErrorCode.getErrorCode(ErrorCode.exception, "", "sdk init error");
//            AgentEventManager.onAdShowEventAgent(adTrackingInfo, "0", adError.printStackTrace());
            return;
        }

        Activity showActivity = activity;
        if (showActivity == null && mContext instanceof Activity) {
            showActivity = (Activity) mContext;
        }

        if (showActivity == null) {
            Log.e(TAG, "Interstitial Show Activity is null.");
        }

//        if (UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext())
//                .getUploadDataLevel() == ATSDK.FORBIDDEN) {
//            AdError adError = ErrorCode.getErrorCode(ErrorCode.dataLevelLowError, "", "");
//            Log.e(TAG, "Show error:" + adError.getDesc());
////            AgentEventManager.onAdShowEventAgent(adTrackingInfo, "0", adError.printStackTrace());
//            return; //如果是FORBIDDEN则不去播放
//        }

        mAdLoadManager.show(showActivity, scenario, mInterListener);
    }

}
