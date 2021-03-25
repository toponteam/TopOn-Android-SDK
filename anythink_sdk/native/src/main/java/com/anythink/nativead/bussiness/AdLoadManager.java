/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.bussiness;

import android.content.Context;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.AdError;
import com.anythink.core.common.AdCacheManager;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.nativead.api.ATNativeOpenSetting;
import com.anythink.nativead.unitgroup.BaseNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

/**
 * Ad Request Manager
 */

public class AdLoadManager extends CommonAdManager<NativeLoadParams> {


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


    public AdCacheInfo showNativeAd(String scenaio) {

        AdCacheInfo adCacheInfo = AdCacheManager.getInstance().getCache(mApplicationContext, mPlacementId);
        if (adCacheInfo != null && adCacheInfo.getAdObject() instanceof BaseNativeAd && adCacheInfo.getBaseAdapter() instanceof CustomNativeAdapter) {
            ATBaseAdAdapter baseAdAdapter = adCacheInfo.getBaseAdapter();
            AdTrackingInfo adTrackingInfo = baseAdAdapter.getTrackingInfo();
            adTrackingInfo.setmScenario(scenaio);
            BaseNativeAd nativeAd = (BaseNativeAd) adCacheInfo.getAdObject();
            nativeAd.setTrackingInfo(adTrackingInfo);
            /**Remove cache**/
            AdCacheManager.getInstance().removeAdCache(mPlacementId, adTrackingInfo.getmUnitGroupUnitId(), adCacheInfo);

            return adCacheInfo;
        }
        return null;
    }



    /**
     * Ad Request
     *
     * @param listener
     */
    public void startLoadAd(Context context, final ATNativeNetworkListener listener) {

        NativeLoadParams loadParams = new NativeLoadParams();
        loadParams.context = context;
        loadParams.listener = listener;
        super.startLoadAd(mApplicationContext, Const.FORMAT.NATIVE_FORMAT, mPlacementId, loadParams);
    }


    @Override
    public CommonMediationManager createFormatMediationManager(NativeLoadParams loadParams) {
        MediationGroupManager mediaionGroupManager = new MediationGroupManager(loadParams.context);
        mediaionGroupManager.setCallbackListener(loadParams.listener);
        return mediaionGroupManager;
    }

    @Override
    public void onCallbackOfferHasExist(NativeLoadParams loadParams, String placementId, String requestId) {
        if (loadParams.listener != null) {
            loadParams.listener.onNativeAdLoaded();
        }
    }

    @Override
    public void onCallbackInternalError(NativeLoadParams loadParams, String placementId, String requestId, AdError adError) {
        if (loadParams.listener != null) {
            loadParams.listener.onNativeAdLoadFail(adError);
        }
    }

    public void setOpenSetting(ATNativeOpenSetting setting, String unityId) {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyByAppIdAndPlaceId(unityId);
        if (placeStrategy != null) {
            setting.isAutoRefresh = placeStrategy.getAutoRefresh() == 1;
            setting.autoRefreshTime = placeStrategy.getAutoRefreshTime();
        }
    }

}
