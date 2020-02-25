package com.anythink.interstitial.business;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.common.AdCacheManager;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.List;
import java.util.Map;

/**
 * 广告请求
 */

public class AdLoadManager extends CommonAdManager {


    public static final String TAG = AdLoadManager.class.getSimpleName();


    public static AdLoadManager getInstance(Context context, String placementId) {

        CommonAdManager adLoadManager = CommonAdManager.getInstance(placementId);
        if (adLoadManager == null || !(adLoadManager instanceof AdLoadManager)) {
            adLoadManager = new AdLoadManager(context, placementId);
            CommonAdManager.addAdManager(placementId, adLoadManager);
        }
        adLoadManager.refreshContext(context);
        return (AdLoadManager) adLoadManager;
    }


    private AdLoadManager(Context context, String placementId) {
        super(context, placementId);
    }



    public void show(final Context context, final String scenario, InterstitialEventListener interstitialEventListener) {

        AdCacheInfo adCacheInfo = isAdReady(context, true);

        if (adCacheInfo != null && adCacheInfo.getBaseAdapter() instanceof CustomInterstitialAdapter) {
            cancelReturnCache(adCacheInfo);
            /**
             * 展示之后关闭定时器
             */
            cancelCountdown();

            AdCacheManager.getInstance().saveShowTime(mApplicationContext, adCacheInfo);
            final CustomInterstitialAdapter customInterstitialAdapter = ((CustomInterstitialAdapter) adCacheInfo.getBaseAdapter());
            if (context instanceof Activity) {
                customInterstitialAdapter.refreshActivityContext((Activity) context);
            }

            AdTrackingInfo adTrackingInfo = adCacheInfo.getBaseAdapter().getTrackingInfo();
            if (adTrackingInfo != null) {
                adTrackingInfo.setCurrentRequestId(mRequestId);
                adTrackingInfo.setmScenario(scenario);
            }

            /**发送展示成功的Tracking**/
            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo);
            customInterstitialAdapter.setCustomInterstitialEventListener(interstitialEventListener);
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    customInterstitialAdapter.show(context);
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
    public void startLoadAd(final Context context, final boolean isAutoRefresh, final Map<String, String> customExtraMap, final ATInterstitialListener listener) {

        loadStragety(mApplicationContext, Const.FORMAT.INTERSTITIAL_FORMAT, mPlacementId, isAutoRefresh, customExtraMap, new PlacementCallback() {
            @Override
            public void onSuccess(String placementId, String requestId, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> unitGroupInfoList) {
                MediationGroupManager mediaionGroupManager = new MediationGroupManager(context);
                mediaionGroupManager.setCallbackListener(listener);
                mediaionGroupManager.setNetworkSettingMap(mSettings); //传入开发者个性化配置
                mediaionGroupManager.setRefresh(isAutoRefresh);
                mediaionGroupManager.loadInterstitialAd(mPlacementId, requestId, placeStrategy, unitGroupInfoList);
                /**将前一个的medationManager里面的callback清空，以免一直持有**/
                if (mCurrentManager != null) {
                    ((MediationGroupManager) mCurrentManager).setCallbackListener(null);
                }
                mHistoryMediationManager.put(requestId, mediaionGroupManager);
                mCurrentManager = mediaionGroupManager;

            }

            @Override
            public void onAdLoaded(String placementId, String requestId) {
                if (listener != null) {
                    listener.onInterstitialAdLoaded();
                }
            }

            @Override
            public void onLoadError(String placementId, String requestId, AdError adError) {
                if (listener != null) {
                    listener.onInterstitialAdLoadFail(adError);
                }
            }
        });
    }

    @Override
    public void startCountdown(PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        CacheCountdownTimer cacheCountdownTimer = new CacheCountdownTimer(unitGroupInfo.getUnitADCacheTime(), unitGroupInfo.getUnitADCacheTime(), unitGroupInfo, adTrackingInfo);
        mCacheCountdownTimer = cacheCountdownTimer;
        mCacheCountdownTimer.start();
    }

}
