package com.anythink.core.common;

import android.content.Context;
import android.os.CountDownTimer;

import com.anythink.core.api.AdError;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.entity.BaseAd;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CustomAdapterFactory;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonCacheCountdownTimer extends CountDownTimer {


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
    public void onFinish() {

        Context context = null;
        if (mUnitgroupInfo == null || mAdTrackingInfo == null) {
            return;
        }


        final CommonAdManager adManager = CommonAdManager.getInstance(mAdTrackingInfo.getmPlacementId());


        context = adManager.getContext();
        if (context == null) {
            return;
        }

        final Context finalContext = context;

        List<PlaceStrategy.UnitGroupInfo> unitGroupInfos = new ArrayList<>();
        unitGroupInfos.add(mUnitgroupInfo);

        if (mUnitgroupInfo.bidType == 1) {
            try {
                HeadBiddingFactory.IHeadBiddingHandler hbHandler = HeadBiddingFactory.createHeadBiddingHandler();
                hbHandler.initHbInfo(context, mAdTrackingInfo.getmRequestId(), mAdTrackingInfo.getmPlacementId(), Integer.parseInt(mAdTrackingInfo.getmAdType()), null, unitGroupInfos);
                hbHandler.startHeadBiddingRequest(new HeadBiddingFactory.IHeadBiddingCallback() {
                    @Override
                    public void onResultCallback(List<PlaceStrategy.UnitGroupInfo> resultList, List<PlaceStrategy.UnitGroupInfo> failList) {
                        if (resultList != null && resultList.size() > 0) {
                            mUnitgroupInfo = resultList.get(0); //refresh unitgroupInfo
                            prepareAdRequest(finalContext);
                        }
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }

        } else {
            prepareAdRequest(context);
        }

    }

    private void prepareAdRequest(Context context) {
        final AnyThinkBaseAdapter adapter = CustomAdapterFactory.createAdapter(mUnitgroupInfo);
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

        CommonLogUtil.i("CacheCountdown", "start to refresh reward ad---");

        adapter.log(Const.LOGKEY.REQUEST, Const.LOGKEY.START, "");


        mPlaceStrategy = AdCacheManager.getInstance().getCachePlacementStrategy(mAdTrackingInfo.getmPlacementId());

        /**Clear AdSource Cache**/
        AdCacheManager.getInstance().forceCleanCache(mAdTrackingInfo.getmPlacementId(), mAdTrackingInfo.getmUnitGroupUnitId());

        mHasGetResult = false;
        /**Start request Ad**/
        startLoadAd(context, starttime, adapter);
    }

    protected void onAdDataLoaded(long starttime, AnyThinkBaseAdapter adapter) {
        AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
        adTrackingInfo.setDataFillTime(System.currentTimeMillis() - starttime);
    }


    /**
     * Filled callback
     *
     * @param context
     * @param starttime
     * @param adapter
     */
    protected void onAdLoaded(Context context, long starttime, AnyThinkBaseAdapter adapter, List<? extends BaseAd> adObjectList) {
        AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();

        if (!mHasGetResult) {
            mHasGetResult = true;

            adTrackingInfo.setFillTime(System.currentTimeMillis() - starttime);
            AdTrackingManager.getInstance(context).addAdTrackingInfo(TrackingV2Loader.AD_REQUEST_SUCCESS_TYPE, adTrackingInfo);

            adapter.log(Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.SUCCESS, "");

        }

        AdCacheManager.getInstance().addCache(adTrackingInfo.getmPlacementId(), adTrackingInfo.getmLevel(), adapter, adObjectList, mUnitgroupInfo.getUnitADCacheTime(), mPlaceStrategy);

        CommonAdManager adManager = CommonAdManager.getInstance(adTrackingInfo.getmPlacementId());

        /**Countdown again**/
        if (adManager != null) {
            adManager.setLoaded();
            adManager.prepareCountdown(adapter, adTrackingInfo.getmRequestId(), adTrackingInfo.getmLevel());
        }

    }


    protected void onAdError(Context context, long startTime, AnyThinkBaseAdapter adapter, AdError adError) {
        AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();

        if (!mHasGetResult) {
            mHasGetResult = true;

            AgentEventManager.onAdsourceLoadFail(adTrackingInfo, 0, adError, System.currentTimeMillis() - startTime);

            adapter.log(Const.LOGKEY.REQUEST_RESULT, Const.LOGKEY.FAIL, adError.printStackTrace());


        }

    }


    protected abstract void startLoadAd(Context activityContext, long startTime, AnyThinkBaseAdapter adapter);
}
