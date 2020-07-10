package com.anythink.rewardvideo.bussiness;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonCacheCountdownTimer;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.rewardvideo.bussiness.utils.CustomRewardVideoAdapterParser;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;

public class CacheCountdownTimer extends CommonCacheCountdownTimer {

    ATMediationSetting mSetting;


    public CacheCountdownTimer(long millisInFuture, long countDownInterval, PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        super(millisInFuture, countDownInterval, unitGroupInfo, adTrackingInfo);
    }

    public void setExtraInfo(ATMediationSetting setting) {
        mSetting = setting;
    }

    @Override
    public void onTick(long millisUntilFinished) {

    }


    @Override
    protected void startLoadAd(final Context activityContext, final long startTime, final AnyThinkBaseAdapter adapter) {
        final Context applicationContext = activityContext.getApplicationContext();
        String placementId = mAdTrackingInfo != null ? mAdTrackingInfo.getmPlacementId() : "";
        CustomRewardVideoAdapterParser.loadRewardVideoAd((Activity) activityContext, (CustomRewardVideoAdapter) adapter, adapter.getmUnitgroupInfo(), PlaceStrategy.getServerExtrasMap(placementId, adapter.getmUnitgroupInfo(), ""), mSetting, new CustomRewardVideoListener() {
            @Override
            public void onRewardedVideoAdDataLoaded(CustomRewardVideoAdapter customRewardVideoAd) {
                onAdDataLoaded(startTime, adapter);
            }

            @Override
            public void onRewardedVideoAdLoaded(CustomRewardVideoAdapter customRewardVideoAd) {
                onAdLoaded(applicationContext, startTime, adapter, null);
            }

            @Override
            public void onRewardedVideoAdFailed(CustomRewardVideoAdapter customRewardVideoAd, AdError adError) {
                onAdError(applicationContext, startTime, adapter, adError);
            }

        });
    }

}
