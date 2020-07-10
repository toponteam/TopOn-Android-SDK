package com.anythink.interstitial.api;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;
import com.anythink.interstitial.business.AdLoadManager;
import com.anythink.interstitial.business.InterstitialEventListener;

import java.util.Map;

/**
 * Copyright (C) 2018 {XX} Science and Technology Co., Ltd.
 *
 * @version V{2.3.0}
 * @Author ：Created by zhoushubin on 2018/9/19.
 * @Email: zhoushubin@salmonads.com
 */
public class ATInterstitial {
    public static String TAG = ATInterstitial.class.getSimpleName();
    public String mUnitid;
    public Context mContext;
    public ATInterstitialListener mInterstitialListener;


    AdLoadManager mAdLoadManager;

    private ATInterstitialListener mInterListener = new ATInterstitialListener() {
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
                load(true);
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

    /***
     * 创建一个Interstitial 广告帮追
     * @param context 上下文
     * @param pUnitid 广告位
     */
    public ATInterstitial(Context context, String pUnitid) {
        mContext = context;

        mUnitid = pUnitid;
        mAdLoadManager = AdLoadManager.getInstance(context, pUnitid);

    }

    public void addSetting(int networkType, ATMediationSetting setting) {
        mAdLoadManager.addSetting(networkType, setting);
    }

    @Deprecated
    public void setCustomExtra(Map<String, String> map) {
    }

    public void load() {
        load(false);
    }

    private void load(final boolean isAutoRefresh) {
        ATSDK.apiLog(mUnitid, Const.LOGKEY.API_INTERSTITIAL, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");
        mAdLoadManager.refreshContext(mContext);
        mAdLoadManager.startLoadAd(mContext, isAutoRefresh, mInterListener);
    }

    private boolean isNeedAutoLoadAfterClose() {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(mUnitid);
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
        ATSDK.apiLog(mUnitid, Const.LOGKEY.API_INTERSTITIAL, Const.LOGKEY.API_ISREADY, String.valueOf(isAdReady), "");
        return isAdReady;
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

    public void show(String scenario) {
        String realScenario = scenario;
        if (CommonSDKUtil.isVailScenario(scenario)) {
            realScenario = scenario;
        }
        controlShow(null, realScenario);
    }

    public void show() {
        controlShow(null, "");
    }

    private void controlShow(Activity activity, String scenario) {
        Context showContext = activity;
        if (showContext == null) {
            showContext = mContext;
        }

        ATSDK.apiLog(mUnitid, Const.LOGKEY.API_INTERSTITIAL, Const.LOGKEY.API_SHOW, Const.LOGKEY.START, "");
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            Log.e(TAG, "Show error: SDK init error!");
//            AdError adError = ErrorCode.getErrorCode(ErrorCode.exception, "", "sdk init error");
//            AgentEventManager.onAdShowEventAgent(adTrackingInfo, "0", adError.printStackTrace());
            return;
        }

//        if (UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext())
//                .getUploadDataLevel() == ATSDK.FORBIDDEN) {
//            AdError adError = ErrorCode.getErrorCode(ErrorCode.dataLevelLowError, "", "");
//            Log.e(TAG, "Show error:" + adError.getDesc());
////            AgentEventManager.onAdShowEventAgent(adTrackingInfo, "0", adError.printStackTrace());
//            return; //如果是FORBIDDEN则不去播放
//        }

        mAdLoadManager.show(showContext, scenario, new InterstitialEventListener(mInterListener));
    }

    @Deprecated
    public void clean() {
//        mAdLoadManager.clean();
    }

    @Deprecated
    public void onPause() {
        mAdLoadManager.onPause();
    }

    @Deprecated
    public void onResume() {
        mAdLoadManager.onResume();
    }

    @Deprecated
    public void onDestory() {
        mAdLoadManager.onDestory();
    }
}
