/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.bussiness;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATCustomLoadListener;
import com.anythink.core.api.ATMediationRequestInfo;
import com.anythink.core.api.AdError;
import com.anythink.core.api.BaseAd;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.CustomAdapterFactory;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;


public class DefaultAdSourceManager {

    private Context mApplcationContext;
    private boolean isLoading;

    boolean isReturResult;

    AdLoadListener mCallbackListener;
//    TimeOutRunnable timeOutRunnable;

    long mStartTime;

    AdCacheInfo mSuccessAdCache;

    public void onSplashAdLoaded(CustomSplashAdapter customSplashAd) {
        if (isReturResult) {
            return;
        }
//        if (timeOutRunnable != null) {
//            SDKContext.getInstance().removeMainThreadRunnable(timeOutRunnable);
//        }

        if (customSplashAd != null) {
            customSplashAd.getTrackingInfo().setFillTime(SystemClock.elapsedRealtime() - mStartTime);
            CommonSDKUtil.printAdTrackingInfoStatusLog(customSplashAd.getTrackingInfo(), Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.SUCCESS, "");
            AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_SUCCESS_TYPE, customSplashAd.getTrackingInfo());

            AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_SUCCESS_TYPE, customSplashAd.getTrackingInfo());

            AdCacheInfo adCacheInfo = new AdCacheInfo();
            adCacheInfo.setRequestLevel(0);
            adCacheInfo.setBaseAdapter(customSplashAd);
            adCacheInfo.setUpdateTime(System.currentTimeMillis());
            adCacheInfo.setCacheTime(10 * 60 * 1000);
            adCacheInfo.setOriginRequestId(customSplashAd.getTrackingInfo().getmRequestId()); //Origin RequestId
            adCacheInfo.setUpStatusCacheTime(10 * 60 * 1000); //Out-Date time of upstatus

            mSuccessAdCache = adCacheInfo;

        }

        isReturResult = true;
        isLoading = false;
        onDevelopLoaded();


    }

    public void onSplashAdFailed(CustomSplashAdapter adapter, final AdError adError) {
        if (isReturResult) {
            return;
        }
//        if (timeOutRunnable != null) {
//            SDKContext.getInstance().removeMainThreadRunnable(timeOutRunnable);
//        }
        if (adapter != null) {
            CommonSDKUtil.printAdTrackingInfoStatusLog(adapter.getTrackingInfo(), Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.FAIL, adError.printStackTrace());
        }
        isReturResult = true;
        isLoading = false;
        onDeveloLoadFail(adapter, adError);

    }

//    public void onSplashAdShowHandle(final CustomSplashAdapter customSplashAd) {
//        if (isRelease) {
//            return;
//        }
//
//        AdTrackingInfo adTrackingInfo = null;
//        if (customSplashAd != null) {
//
//            adTrackingInfo = customSplashAd.getTrackingInfo();
//            long timestamp = System.currentTimeMillis();
//            adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), "", timestamp));
//
////                TrackingInfoUtil.fillTrackingInfoShowTime(mApplcationContext, adTrackingInfo);
//
//            AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo, timestamp);
//
//            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");
//
//        }
//
//        if (mCallbackListener != null) {
//            mCallbackListener.onAdShow(ATAdInfo.fromAdapter(customSplashAd));
//        }
//
//    }

//    public void onSplashAdClickedHandle(CustomSplashAdapter customSplashAd) {
//        if (isRelease) {
//            return;
//        }
//        if (customSplashAd != null) {
//            AdTrackingInfo adTrackingInfo = customSplashAd.getTrackingInfo();
//
//            AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);
//
//            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");
//
//        }
//
//        if (mCallbackListener != null) {
//            mCallbackListener.onAdClick(ATAdInfo.fromAdapter(customSplashAd));
//        }
//    }

//    public void onSplashAdDismissHandle(CustomSplashAdapter customSplashAd) {
//        if (isRelease) {
//            return;
//        }
//        callbackDismiss(customSplashAd);
//    }


    public DefaultAdSourceManager(Context context) {
        mApplcationContext = context.getApplicationContext();
    }

    public void onDevelopLoaded() {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallbackListener != null) {
                    mCallbackListener.onCallbackAdLoaded();
                }
                mCallbackListener = null;
            }
        });

    }

    protected boolean isLoading() {
        return isLoading;
    }

    String mRequestId;
    String mPlacementId;

    public void startRequestAd(Context context, String placementId, String requestId, ATMediationRequestInfo atMediationRequestInfo, AdLoadListener atSplashAdListener, int timeout) {
        mCallbackListener = atSplashAdListener;

        mRequestId = requestId;
        mPlacementId = placementId;

        AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
        adTrackingInfo.setmPlacementId(placementId);
        adTrackingInfo.setmRequestId(requestId);
        adTrackingInfo.setmNetworkType(atMediationRequestInfo.getNetworkFirmId());
        adTrackingInfo.setmAdType(Const.FORMAT.SPLASH_FORMAT);
        adTrackingInfo.setmUnitGroupUnitId(TextUtils.isEmpty(atMediationRequestInfo.getAdSourceId()) ? "0" : atMediationRequestInfo.getAdSourceId());
        adTrackingInfo.setAsid("0");
        adTrackingInfo.setmIsLoad(true);


        try {
            ATBaseAdAdapter anyThinkBaseAdapter = CustomAdapterFactory.create(atMediationRequestInfo.getClassName());
            if (anyThinkBaseAdapter instanceof CustomSplashAdapter) {
                CustomSplashAdapter splashAdapter = (CustomSplashAdapter) anyThinkBaseAdapter;
                splashAdapter.setFetchAdTimeout(timeout);
                isLoading = true;
                isReturResult = false;

                mStartTime = SystemClock.elapsedRealtime();

                adTrackingInfo.setNetworkName(anyThinkBaseAdapter.getNetworkName());
                adTrackingInfo.setRequestType(AdTrackingInfo.HANDLE_REQUEST);

                anyThinkBaseAdapter.setTrackingInfo(adTrackingInfo);
                CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.REQUEST, Const.LOGKEY.START, "");
                AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_LOAD_TYPE, adTrackingInfo);

                AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_TYPE, adTrackingInfo);
//                timeOutRunnable = new TimeOutRunnable((CustomSplashAdapter) anyThinkBaseAdapter);
                //Only for protect there is no result to callback.
//                SDKContext.getInstance().runOnMainThreadDelayed(timeOutRunnable, 10000);
//                ((CustomSplashAdapter) anyThinkBaseAdapter).initSplashImpressionListener(new SplashEventListener((CustomSplashAdapter) anyThinkBaseAdapter));
                anyThinkBaseAdapter.internalLoad(context, atMediationRequestInfo.getRequestParamMap(), PlacementAdManager.getInstance().getPlacementLocalSettingMap(placementId), new SplashCustomLoadListener((CustomSplashAdapter) anyThinkBaseAdapter));
            } else {
                throw new Exception("The class isn't instanceof CustomSplashAdapter");
            }
        } catch (Throwable e) {
            if (mCallbackListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.adapterNotExistError, "", e.getMessage());
                mCallbackListener.onCallbackNoAdError(adError);
            }
            mCallbackListener = null;
        }

    }

    public void onDeveloLoadFail(final CustomSplashAdapter splashAdapter, final AdError adError) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (splashAdapter != null) {
                    splashAdapter.destory();
                }

                if (mCallbackListener != null) {
                    mCallbackListener.onCallbackNoAdError(adError);
                }
                mCallbackListener = null;
            }
        });
    }

//    private void callbackDismiss(CustomSplashAdapter splashAdapter) {
//        if (!hasDismiss) {
//            hasDismiss = true;
//            if (splashAdapter != null && splashAdapter.getTrackingInfo() != null) {
//                CommonSDKUtil.printAdTrackingInfoStatusLog(splashAdapter.getTrackingInfo(), Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");
//            }
//
//            if (mCallbackListener != null) {
//                mCallbackListener.onAdDismiss(ATAdInfo.fromAdapter(splashAdapter));
//            }
//
//            if (splashAdapter != null) {
//                splashAdapter.destory();
//            }
//
//            mCallbackListener = null;
//        }
//    }


    public void release() {
        mCallbackListener = null;
    }

    public void removeCache() {
        mSuccessAdCache = null;
    }

//    class TimeOutRunnable implements Runnable {
//        CustomSplashAdapter adapter;
//
//        TimeOutRunnable(CustomSplashAdapter adapter) {
//            this.adapter = adapter;
//        }
//
//        @Override
//        public void run() {
//            onSplashAdFailed(adapter, ErrorCode.getErrorCode(ErrorCode.timeOutError, "", ""));
//            release();
//        }
//    }

    private class SplashCustomLoadListener implements ATCustomLoadListener {
        CustomSplashAdapter splashAdapter;

        public SplashCustomLoadListener(CustomSplashAdapter splashAdapter) {
            this.splashAdapter = splashAdapter;
        }

        @Override
        public void onAdDataLoaded() {

        }

        @Override
        public void onAdCacheLoaded(BaseAd... baseAds) {
            onSplashAdLoaded(splashAdapter);
        }

        @Override
        public void onAdLoadError(String errorCode, String errorMsg) {
            onSplashAdFailed(splashAdapter, ErrorCode.getErrorCode(ErrorCode.noADError, errorCode, errorMsg));
        }
    }

    /**
     * Callback timeout in ATSplash
     */
    public void callbackTimeout() {
        AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
        adTrackingInfo.setmPlacementId(mPlacementId);
        adTrackingInfo.setmRequestId(mRequestId);
        adTrackingInfo.setmAdType(Const.FORMAT.SPLASH_FORMAT);
        adTrackingInfo.setAsid("0");
        adTrackingInfo.setmIsLoad(true);
        AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, ErrorCode.getErrorCode(ErrorCode.timeOutError, "", "Splash FetchAd Timeout."));

    }

    public AdCacheInfo getReadySplashAdCache() {
        if (mSuccessAdCache != null && mSuccessAdCache.getShowTime() <= 0) {
            return mSuccessAdCache;
        }
        return null;
    }
//
//    private class SplashEventListener implements CustomSplashEventListener {
//        CustomSplashAdapter splashAdapter;
//
//        public SplashEventListener(CustomSplashAdapter splashAdapter) {
//            this.splashAdapter = splashAdapter;
//        }
//
//        @Override
//        public void onSplashAdShow() {
//            onSplashAdShowHandle(splashAdapter);
//        }
//
//        @Override
//        public void onSplashAdClicked() {
//            onSplashAdClickedHandle(splashAdapter);
//        }
//
//        @Override
//        public void onSplashAdDismiss() {
//            onSplashAdDismissHandle(splashAdapter);
//        }
//    }
}
