/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common;

import android.content.Context;
import android.os.CountDownTimer;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATCustomLoadListener;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.api.BaseAd;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.core.common.utils.CustomAdapterFactory;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommonCacheCountdownTimer extends CountDownTimer {
    private final String TAG = getClass().getSimpleName();

    protected PlaceStrategy.UnitGroupInfo mUnitgroupInfo;
    protected AdTrackingInfo mAdTrackingInfo;
    protected PlaceStrategy mPlaceStrategy;


    public CommonCacheCountdownTimer(long millisInFuture, long countDownInterval, PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        super(millisInFuture, countDownInterval);
        mUnitgroupInfo = unitGroupInfo;
        mAdTrackingInfo = adTrackingInfo;
    }

    boolean mHasGetResult = false;

    @Override
    public void onTick(long millisUntilFinished) {

    }

    @Override
    public void onFinish() {

        Context context = null;
        if (mUnitgroupInfo == null || mAdTrackingInfo == null) {
            return;
        }


        final CommonAdManager adManager = PlacementAdManager.getInstance().getAdManager(mAdTrackingInfo.getmPlacementId());


        context = adManager.getContext();
        if (context == null) {
            return;
        }

        prepareAdRequest(context);

    }

    private void prepareAdRequest(Context context) {
        final ATBaseAdAdapter adapter = CustomAdapterFactory.createAdapter(mUnitgroupInfo);
        if (adapter == null) {
            return;
        }

        mAdTrackingInfo.setRequestType(AdTrackingInfo.AUTO_REQUEST);
        mAdTrackingInfo.setLoadStatus(AdTrackingInfo.NORMAL_CALLBACK);
        mAdTrackingInfo.setFlag(AdTrackingInfo.NO_SHOW_CACHE);

        adapter.setTrackingInfo(mAdTrackingInfo);
        adapter.setmUnitgroupInfo(mUnitgroupInfo);

        final long starttime = System.currentTimeMillis();


        AdTrackingManager.getInstance(context).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_TYPE, mAdTrackingInfo);

        CommonLogUtil.i(TAG, "start to refresh Ad---");

        CommonSDKUtil.printAdTrackingInfoStatusLog(mAdTrackingInfo, Const.LOGKEY.REQUEST, Const.LOGKEY.START, "");


        mPlaceStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(mAdTrackingInfo.getmPlacementId());

        /**Clear AdSource Cache**/
        AdCacheManager.getInstance().forceCleanCache(mAdTrackingInfo.getmPlacementId(), mAdTrackingInfo.getmUnitGroupUnitId());

        mHasGetResult = false;

        Map<String, Object> localMap = PlacementAdManager.getInstance().getPlacementLocalSettingMap(mAdTrackingInfo.getmPlacementId());
        /**Start request Ad**/
        adapter.internalLoad(context
                , mPlaceStrategy.getServerExtrasMap(mAdTrackingInfo.getmPlacementId(), mAdTrackingInfo.getmRequestId(), adapter.getmUnitgroupInfo())
                , localMap, new CustomAdapterLoadListener(starttime, adapter));
    }

    protected void onAdDataLoaded(long starttime, AnyThinkBaseAdapter adapter) {
        AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
        adTrackingInfo.setDataFillTime(System.currentTimeMillis() - starttime);
    }


    /**
     * Filled callback
     *
     * @param starttime
     * @param adapter
     */
    protected void onAdLoaded(long starttime, ATBaseAdAdapter adapter, List<? extends BaseAd> adObjectList) {
        AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();

        if (!mHasGetResult) {
            mHasGetResult = true;

            adTrackingInfo.setFillTime(System.currentTimeMillis() - starttime);
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_SUCCESS_TYPE, adTrackingInfo);

            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.SUCCESS, "");

        }

        AdCacheManager.getInstance().addCache(adTrackingInfo.getmPlacementId(), adTrackingInfo.getRequestLevel(), adapter, adObjectList, mUnitgroupInfo.getUnitADCacheTime(), mPlaceStrategy);


    }


    protected void onAdError(long startTime, AnyThinkBaseAdapter adapter, AdError adError) {
        AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();

        if (!mHasGetResult) {
            mHasGetResult = true;

            AgentEventManager.onAdsourceLoadFail(adTrackingInfo, 0, adError, System.currentTimeMillis() - startTime);
            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.FAIL, adError.printStackTrace());

        }
    }

    private class CustomAdapterLoadListener implements ATCustomLoadListener {
        ATBaseAdAdapter baseAdAdapter;
        long startTime;

        private CustomAdapterLoadListener(long startTime, ATBaseAdAdapter baseAdAdapter) {
            this.startTime = startTime;
            this.baseAdAdapter = baseAdAdapter;
        }

        @Override
        public void onAdDataLoaded() {
            CommonCacheCountdownTimer.this.onAdDataLoaded(startTime, baseAdAdapter);
        }

        @Override
        public void onAdCacheLoaded(BaseAd... baseAds) {
            CommonCacheCountdownTimer.this.onAdLoaded(startTime, baseAdAdapter, baseAds != null ? Arrays.asList(baseAds) : null);
            if (baseAdAdapter != null) {
                baseAdAdapter.releaseLoadResource();
            }
        }

        @Override
        public void onAdLoadError(String errorCode, String errorMsg) {
            CommonCacheCountdownTimer.this.onAdError(startTime, baseAdAdapter, ErrorCode.getErrorCode(ErrorCode.noADError, errorCode, errorMsg));
            if (baseAdAdapter != null) {
                baseAdAdapter.releaseLoadResource();
            }
        }
    }

}
