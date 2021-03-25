/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.bussiness;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import com.anythink.core.api.ATMediationRequestInfo;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.AdCacheManager;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;

/**
 * Ad Request Manager
 */

public class AdLoadManager extends CommonAdManager<SplashLoadParams> {

    DefaultAdSourceManager defaultAdSourceManager;

    public AdLoadManager(Context context, String placementId) {
        super(context, placementId);
    }


    public static AdLoadManager getInstance(Context context, String placementId) {

        CommonAdManager adLoadManager = PlacementAdManager.getInstance().getAdManager(placementId);
        if (adLoadManager == null || !(adLoadManager instanceof AdLoadManager)) {
            adLoadManager = new AdLoadManager(context, placementId);
            PlacementAdManager.getInstance().addAdManager(placementId, adLoadManager);
        }
        return (AdLoadManager) adLoadManager;
    }

    /**
     * Ad Request
     *
     * @param listener
     */
    public void startLoadAd(Context context, ATMediationRequestInfo defaultRequestInfo, final AdLoadListener listener, int timeout) {
        SplashLoadParams loadParams = new SplashLoadParams();
        loadParams.context = context;
        loadParams.listener = listener;
        loadParams.defaultRequestInfo = defaultRequestInfo;
        loadParams.timeout = timeout;

        super.startLoadAd(mApplicationContext, Const.FORMAT.SPLASH_FORMAT, mPlacementId, loadParams);

    }

    @Override
    public void onCreateRequestId(String requestId, SplashLoadParams formatLoadParams) {
        formatLoadParams.listener.setRequestId(requestId);
    }

    @Override
    public CommonMediationManager createFormatMediationManager(SplashLoadParams formatLoadParams) {
        MediationGroupManager mediationManager = new MediationGroupManager(formatLoadParams.context);
        mediationManager.setFetchAdTimeout(formatLoadParams.timeout);
        mediationManager.setCallbackListener(formatLoadParams.listener);
        return mediationManager;
    }

    @Override
    protected AdCacheInfo isAdReady(Context context, boolean isShowCall) {
        AdCacheInfo defaultAdCacheInfo = defaultAdSourceManager != null ? defaultAdSourceManager.getReadySplashAdCache() : null;
        if (defaultAdCacheInfo != null) {
            return defaultAdCacheInfo;
        }
        return super.isAdReady(context, isShowCall);
    }

    public synchronized void show(final Activity activity, final ViewGroup container, final AdEventListener listener) {
        final AdCacheInfo adCacheInfo = isAdReady(activity, true);
        if (adCacheInfo == null) {
            Log.e(Const.RESOURCE_HEAD, "Splash No Cache.");
            return;
        }

        if (adCacheInfo != null && adCacheInfo.getBaseAdapter() instanceof CustomSplashAdapter) {
            notifyNewestCacheHasBeenShow(adCacheInfo);
            /**
             * Cancel countdown after showing
             */
            cancelCountdown();

            /**Mark ad has been showed**/
            adCacheInfo.setShowTime(adCacheInfo.getShowTime() + 1);

            /**Remove default cache**/
            if (defaultAdSourceManager != null && defaultAdSourceManager.getReadySplashAdCache() == adCacheInfo) {
                defaultAdSourceManager.removeCache();
            }

            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    final CustomSplashAdapter customSplashAdapter = ((CustomSplashAdapter) adCacheInfo.getBaseAdapter());
                    if (activity != null) {
                        customSplashAdapter.refreshActivityContext(activity);
                    }

                    final AdTrackingInfo adTrackingInfo = adCacheInfo.getBaseAdapter().getTrackingInfo();

                    long timestamp = System.currentTimeMillis();

                    if (adTrackingInfo != null) {
                        adTrackingInfo.setCurrentRequestId(mRequestId);
                        adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));

                        /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
                        TrackingInfoUtil.fillTrackingInfoShowTime(mApplicationContext, adTrackingInfo);
                    }

                    if (customSplashAdapter.getmUnitgroupInfo() != null) {
                        AdCacheManager.getInstance().saveShowTimeToDisk(mApplicationContext, adCacheInfo);
                    }

                    /**Send Tracking**/
                    AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo, timestamp);
                    SDKContext.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            customSplashAdapter.internalShow(activity, container, new SplashEventListener(customSplashAdapter, listener));
                        }
                    });
                }
            });

        }

    }

    @Override
    public boolean isInDefaultAdSourceLoading() {
        return defaultAdSourceManager != null && defaultAdSourceManager.isLoading();
    }

    @Override
    public boolean startDefaultAdSourceLoading(String placementId, String requestId, SplashLoadParams loadParam) {
        defaultAdSourceManager = new DefaultAdSourceManager(mApplicationContext);
        defaultAdSourceManager.startRequestAd(loadParam.context, placementId, requestId, loadParam.defaultRequestInfo, loadParam.listener, loadParam.timeout);
        return true;
    }

    @Override
    public void onCallbackOfferHasExist(SplashLoadParams formatLoadParams, String placementId, String requestId) {
        if (formatLoadParams.listener != null) {
            formatLoadParams.listener.onCallbackAdLoaded();
        }
    }

    @Override
    public void onCallbackInternalError(SplashLoadParams formatLoadParams, String placementId, String requestId, AdError adError) {
        if (formatLoadParams.listener != null) {
            formatLoadParams.listener.onCallbackNoAdError(adError);
        }
    }

//    /**
//     * Release the current MediationManager
//     */
//    public void releaseMediationManager() {
//        if (defaultAdSourceManager != null) {
//            defaultAdSourceManager.release();
//            defaultAdSourceManager = null;
//        }
//
//        String requestId = ShowWaterfallManager.getInstance().getWaterFallNewestRequestId(mPlacementId);
//        if (!TextUtils.isEmpty(requestId)) {
//            CommonMediationManager mediationGroupManager = mHistoryMediationManager.get(requestId);
//            if (mediationGroupManager != null) {
//                mediationGroupManager.release();
//            }
//
//        }
//
//    }

    /**
     * Timeout & Release
     */
    public void sendRequestTimeoutAgent(String requestId) {
        if (defaultAdSourceManager != null) {
            defaultAdSourceManager.callbackTimeout();
            defaultAdSourceManager.release();
            defaultAdSourceManager = null;
        }

        if (!TextUtils.isEmpty(requestId)) {
            CommonMediationManager mediationGroupManager = mHistoryMediationManager.get(requestId);
            mHistoryMediationManager.remove(requestId); // remove request mediation manager
            if (mediationGroupManager != null) {
                if (mediationGroupManager instanceof MediationGroupManager) {
                    ((MediationGroupManager) mediationGroupManager).callbackTimeout();
                    mediationGroupManager.release();
                }
            } else {
                AdTrackingInfo adTrackingInfo = new AdTrackingInfo();
                adTrackingInfo.setmPlacementId(mPlacementId);
                adTrackingInfo.setmRequestId(requestId);
                adTrackingInfo.setmAdType(Const.FORMAT.SPLASH_FORMAT);
                adTrackingInfo.setAsid("0");
                adTrackingInfo.setmIsLoad(true);
                AgentEventManager.onAgentForATToAppLoadFail(adTrackingInfo, ErrorCode.getErrorCode(ErrorCode.timeOutError, "", "Splash FetchAd Timeout."));
            }

        }
    }

}
