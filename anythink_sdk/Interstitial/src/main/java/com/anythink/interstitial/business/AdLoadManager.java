/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.interstitial.business;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.AdError;
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
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;

/**
 * Interstital Request Manager
 */

public class AdLoadManager extends CommonAdManager<InterstitialLoadParams> {


    public static final String TAG = AdLoadManager.class.getSimpleName();


    public static AdLoadManager getInstance(Context context, String placementId) {

        CommonAdManager adLoadManager = PlacementAdManager.getInstance().getAdManager(placementId);
        if (adLoadManager == null || !(adLoadManager instanceof AdLoadManager)) {
            adLoadManager = new AdLoadManager(context, placementId);
            PlacementAdManager.getInstance().addAdManager(placementId, adLoadManager);
        }
        return (AdLoadManager) adLoadManager;
    }


    private AdLoadManager(Context context, String placementId) {
        super(context, placementId);
    }


    public synchronized void show(final Activity activity, final String scenario, final ATInterstitialListener interstitialEventListener) {

        final AdCacheInfo adCacheInfo = isAdReady(activity, true);

        if (adCacheInfo != null && adCacheInfo.getBaseAdapter() instanceof CustomInterstitialAdapter) {
            notifyNewestCacheHasBeenShow(adCacheInfo);
            /**
             * Cancel countdown after showing
             */
            cancelCountdown();

            /**Mark ad has been showed**/
            adCacheInfo.setShowTime(adCacheInfo.getShowTime() + 1);

            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    final CustomInterstitialAdapter customInterstitialAdapter = ((CustomInterstitialAdapter) adCacheInfo.getBaseAdapter());
                    if (activity != null) {
                        customInterstitialAdapter.refreshActivityContext(activity);
                    }

                    final AdTrackingInfo adTrackingInfo = adCacheInfo.getBaseAdapter().getTrackingInfo();

                    long timestamp = System.currentTimeMillis();

                    if (adTrackingInfo != null) {
                        adTrackingInfo.setCurrentRequestId(mRequestId);
                        adTrackingInfo.setmScenario(scenario);
                        adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));

                        /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
                        TrackingInfoUtil.fillTrackingInfoShowTime(mApplicationContext, adTrackingInfo);
                    }


                    AdCacheManager.getInstance().saveShowTimeToDisk(mApplicationContext, adCacheInfo);

                    /**Send Tracking**/
                    AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo, timestamp);
                    SDKContext.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            customInterstitialAdapter.setScenario(scenario);
                            customInterstitialAdapter.internalShow(activity, new InterstitialEventListener(customInterstitialAdapter, interstitialEventListener));
                        }
                    });
                }
            });

        }

    }

    public void onPause() {

    }

    public void onResume() {
    }

    public void onDestory() {
    }


    /**
     * 广告请求
     *
     * @param listener
     */
    public void startLoadAd(final Context context, final boolean isAutoRefresh, final ATInterstitialListener listener) {

        InterstitialLoadParams interstitialLoadParams = new InterstitialLoadParams();
        interstitialLoadParams.context = context;
        interstitialLoadParams.listener = listener;
        interstitialLoadParams.isRefresh = isAutoRefresh;

        super.startLoadAd(mApplicationContext, Const.FORMAT.INTERSTITIAL_FORMAT, mPlacementId, interstitialLoadParams);

    }


    @Override
    public CommonMediationManager createFormatMediationManager(InterstitialLoadParams loadParams) {
        MediationGroupManager mediaionGroupManager = new MediationGroupManager(loadParams.context);
        mediaionGroupManager.setCallbackListener(loadParams.listener);
        mediaionGroupManager.setRefresh(loadParams.isRefresh);
        return mediaionGroupManager;
    }

    @Override
    public void onCallbackOfferHasExist(InterstitialLoadParams loadParams, String placementId, String requestId) {
        if (loadParams.listener != null) {
            loadParams.listener.onInterstitialAdLoaded();
        }
    }

    @Override
    public void onCallbackInternalError(InterstitialLoadParams loadParams, String placementId, String requestId, AdError adError) {
        if (loadParams.listener != null) {
            loadParams.listener.onInterstitialAdLoadFail(adError);
        }
    }


}
