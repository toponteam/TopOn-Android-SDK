package com.anythink.nativead.bussiness;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonCacheCountdownTimer;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.nativead.bussiness.utils.CustomNativeAdapterParser;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import java.util.List;
import java.util.Map;

public class CacheCountdownTimer extends CommonCacheCountdownTimer {

    Map<String, Object> mLocalMap;

    public CacheCountdownTimer(long millisInFuture, long countDownInterval, PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        super(millisInFuture, countDownInterval, unitGroupInfo, adTrackingInfo);
    }

    public void setCacheAdapter(Map<String, Object> setting) {
        mLocalMap = setting;
    }

    @Override
    public void onTick(long millisUntilFinished) {

    }


    @Override
    protected void startLoadAd(final Context activityContext, final long startTime, final AnyThinkBaseAdapter baseAdapter) {
        final Context applicationContext = activityContext.getApplicationContext();
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(applicationContext).getPlaceStrategyByAppIdAndPlaceId(mAdTrackingInfo.getmPlacementId());
        String placementId = mAdTrackingInfo != null ? mAdTrackingInfo.getmPlacementId() : "";
        CustomNativeAdapterParser.loadNativeAd(activityContext, (CustomNativeAdapter) baseAdapter, placeStrategy, mUnitgroupInfo, PlaceStrategy.getServerExtrasMap(placementId, mUnitgroupInfo, ""), mLocalMap, new CustomNativeListener() {
            @Override
            public void onNativeAdLoaded(CustomNativeAdapter adapter, List<CustomNativeAd> nativeAd) {
                onAdLoaded(applicationContext, startTime, adapter, nativeAd);
            }

            @Override
            public void onNativeAdFailed(CustomNativeAdapter adapter, AdError error) {
                onAdError(applicationContext, startTime, adapter, error);
            }
        });
    }

}
