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
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATMediationRequestInfo;
import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.ShowWaterfallManager;
import com.anythink.core.common.base.Const;
import com.anythink.splashad.api.ATSplashAdListener;

/**
 * Ad Request Manager
 */

public class AdLoadManager extends CommonAdManager<SplashLoadParams> {

    DefaultAdSourceManager defaultAdSourceManager;

    public AdLoadManager(Context context, String placementId) {
        super(context, placementId);
    }


    public static AdLoadManager getInstance(Activity context, String placementId) {

        CommonAdManager adLoadManager = PlacementAdManager.getInstance().getAdManager(placementId);
        if (adLoadManager == null || !(adLoadManager instanceof AdLoadManager)) {
            adLoadManager = new AdLoadManager(context, placementId);
            PlacementAdManager.getInstance().addAdManager(placementId, adLoadManager);
        }
        adLoadManager.refreshContext(context);
        return (AdLoadManager) adLoadManager;
    }

    /**
     * Ad Request
     *
     * @param listener
     */
    public void startLoadAd(Activity activity, final ViewGroup container, final View skipView, ATMediationRequestInfo defaultRequestInfo, final ATSplashAdListener listener) {
        SplashLoadParams loadParams = new SplashLoadParams();
        loadParams.activity = activity;
        loadParams.containerView = container;
        loadParams.listener = listener;
        loadParams.defaultRequestInfo = defaultRequestInfo;

        super.startLoadAd(mApplicationContext, Const.FORMAT.SPLASH_FORMAT, mPlacementId, loadParams);

    }


    @Override
    public CommonMediationManager createFormatMediationManager(SplashLoadParams formatLoadParams) {
        MediationGroupManager mediationManager = new MediationGroupManager(formatLoadParams.activity);
        mediationManager.setCallbackListener(formatLoadParams.listener);
        mediationManager.setContainerView(formatLoadParams.containerView);
        return mediationManager;
    }


    @Override
    public boolean isInDefaultAdSourceLoading() {
        return defaultAdSourceManager != null && defaultAdSourceManager.isLoading();
    }

    @Override
    public boolean startDefaultAdSourceLoading(String placementId, String requestId, SplashLoadParams loadParam) {
        defaultAdSourceManager = new DefaultAdSourceManager(mApplicationContext);
        defaultAdSourceManager.startRequestAd(loadParam.activity, placementId, requestId, loadParam.containerView, loadParam.defaultRequestInfo, loadParam.listener);
        return true;
    }

    @Override
    public void onCallbackOfferHasExist(SplashLoadParams formatLoadParams, String placementId, String requestId) {
        if (formatLoadParams.listener != null) {
            formatLoadParams.listener.onAdLoaded();
        }
    }

    @Override
    public void onCallbacInternalError(SplashLoadParams formatLoadParams, String placementId, String requestId, AdError adError) {
        if (formatLoadParams.listener != null) {
            formatLoadParams.listener.onNoAdError(adError);
        }
    }

    /**
     * Release the current MediationManager
     */
    public void releaseMediationManager() {
        if (defaultAdSourceManager != null) {
            defaultAdSourceManager.release();
            defaultAdSourceManager = null;
        }

        String requestId = ShowWaterfallManager.getInstance().getWaterFallNewestRequestId(mPlacementId);
        if (!TextUtils.isEmpty(requestId)) {
            CommonMediationManager mediationGroupManager = mHistoryMediationManager.get(requestId);
            if (mediationGroupManager != null) {
                mediationGroupManager.release();
                mHistoryMediationManager.remove(requestId);
            }

        }

    }

}
