/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.rewardvideo.bussiness;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATAdInfo;
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
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.TrackingInfoUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

/**
 * Ad Request Manager
 */

public class AdLoadManager extends CommonAdManager<RewardedVideoLoadParams> {


    public static final String TAG = AdLoadManager.class.getSimpleName();

    String mUserId;
    String mCustomData;


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

    public synchronized void show(final Activity activity, final String scenario, final ATRewardVideoListener listener) {
        final AdCacheInfo adCacheInfo = isAdReady(activity, true);
        if (adCacheInfo == null) {
            AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "No Cache.");

            if (listener != null) {
                listener.onRewardedVideoAdPlayFailed(adError, ATAdInfo.fromAdapter(null));
            }

            return;
        }


        if (adCacheInfo != null && adCacheInfo.getBaseAdapter() instanceof CustomRewardVideoAdapter) {
            notifyNewestCacheHasBeenShow(adCacheInfo);
            /**
             * Remove CountDown
             */
            cancelCountdown();

            /**Mark ad has been showed**/
            adCacheInfo.setShowTime(adCacheInfo.getShowTime() + 1);

            //Tracking and save show-time
            TaskManager.getInstance().run_proxy(new Runnable() {
                @Override
                public void run() {
                    final AdTrackingInfo adTrackingInfo = adCacheInfo.getBaseAdapter().getTrackingInfo();
                    long timestamp = System.currentTimeMillis();

                    if (adTrackingInfo != null) {
                        adTrackingInfo.setCurrentRequestId(mRequestId);
                        adTrackingInfo.setmScenario(scenario);
                        adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));

                        /**Must set before AdCacheManager.saveShowTime()，don't suggest to do it in UI-Thread**/
                        TrackingInfoUtil.fillTrackingInfoShowTime(mApplicationContext, adTrackingInfo);
                    }

                    AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo, timestamp);

                    AdCacheManager.getInstance().saveShowTimeToDisk(mApplicationContext, adCacheInfo);


                    final CustomRewardVideoAdapter customRewardVideoAdapter = ((CustomRewardVideoAdapter) adCacheInfo.getBaseAdapter());
                    if (activity != null) {
                        customRewardVideoAdapter.refreshActivityContext(activity);
                    }

                    SDKContext.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            customRewardVideoAdapter.setScenario(scenario);
                            customRewardVideoAdapter.internalShow(activity, new RewardedVideoEventListener(customRewardVideoAdapter, listener));
                        }
                    });
                }
            });

        }

    }

    /**
     * Ad Request
     *
     * @param listener
     */
    public void startLoadAd(final Context context, final boolean isAutoRefresh, final ATRewardVideoListener listener) {
        RewardedVideoLoadParams loadParams = new RewardedVideoLoadParams();
        loadParams.isRefresh = isAutoRefresh;
        loadParams.userId = mUserId;
        loadParams.userData = mCustomData;
        loadParams.listener = listener;
        loadParams.context = context;

        super.startLoadAd(mApplicationContext, Const.FORMAT.REWARDEDVIDEO_FORMAT, mPlacementId, loadParams);
    }


    @Override
    public CommonMediationManager createFormatMediationManager(RewardedVideoLoadParams formatLoadParams) {
        MediationGroupManager mediaionGroupManager = new MediationGroupManager(formatLoadParams.context);
        mediaionGroupManager.setUserData(mUserId, mCustomData);
        mediaionGroupManager.setCallbackListener(formatLoadParams.listener);
        mediaionGroupManager.setRefresh(formatLoadParams.isRefresh);
        return mediaionGroupManager;
    }

    @Override
    public void onCallbackOfferHasExist(RewardedVideoLoadParams formatLoadParams, String placementId, String requestId) {
        if (formatLoadParams.listener != null) {
            formatLoadParams.listener.onRewardedVideoAdLoaded();
        }
    }

    @Override
    public void onCallbackInternalError(RewardedVideoLoadParams formatLoadParams, String placementId, String requestId, AdError adError) {
        if (formatLoadParams.listener != null) {
            formatLoadParams.listener.onRewardedVideoAdFailed(adError);
        }
    }

    public void setUserData(String userId, String customData) {
        mUserId = userId;
        mCustomData = customData;
    }


}
