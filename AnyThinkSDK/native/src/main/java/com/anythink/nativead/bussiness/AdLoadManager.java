package com.anythink.nativead.bussiness;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.common.AdCacheManager;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.nativead.api.ATNativeOpenSetting;
import com.anythink.nativead.unitgroup.BaseNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import java.util.List;
import java.util.Map;

/**
 * Ad Request Manager
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


    public AdCacheInfo showNativeAd() {

        AdCacheInfo adCacheInfo = AdCacheManager.getInstance().getCache(mApplicationContext, mPlacementId, null);
        if (adCacheInfo != null && adCacheInfo.getAdObject() instanceof BaseNativeAd && adCacheInfo.getBaseAdapter() instanceof CustomNativeAdapter) {
            BaseNativeAd nativeAd = (BaseNativeAd) adCacheInfo.getAdObject();
            nativeAd.setTrackingInfo(adCacheInfo.getBaseAdapter().getTrackingInfo());
            return adCacheInfo;
        }
        return null;
    }


    Map<String, Object> mLocalMap;

    /**
     * Ad Request
     *
     * @param listener
     */
    public void startLoadAd(final Map<String, Object> localMap, final Map<String, String> customExtraMap, final ATNativeNetworkListener listener) {

        mLocalMap = localMap;
        loadStragety(mApplicationContext, Const.FORMAT.NATIVE_FORMAT, mPlacementId, false, customExtraMap, new PlacementCallback() {
            @Override
            public void onSuccess(String placementId, String requestId, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> unitGroupInfoList) {
                MediationGroupManager mediaionGroupManager = new MediationGroupManager(mActivityRef.get());
                mediaionGroupManager.setCallbackListener(listener);
                mediaionGroupManager.setLocalMap(mLocalMap);
                mediaionGroupManager.loadNativeAd(mPlacementId, requestId, placeStrategy, unitGroupInfoList);
                mHistoryMediationManager.put(requestId, mediaionGroupManager);
                /**Clear listener in the old MediationManager**/
                if (mCurrentManager != null) {
                    ((MediationGroupManager) mCurrentManager).setCallbackListener(null);
                }
                mCurrentManager = mediaionGroupManager;

            }

            @Override
            public void onAdLoaded(String placementId, String requestId) {
                if (listener != null) {
                    listener.onNativeAdLoaded();
                }
            }

            @Override
            public void onLoadError(String placementId, String requestId, AdError adError) {
                if (listener != null) {
                    listener.onNativeAdLoadFail(adError);
                }
            }


        });
    }

    @Override
    public void startCountdown(PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        CacheCountdownTimer cacheCountdownTimer = new CacheCountdownTimer(unitGroupInfo.getUnitADCacheTime(), unitGroupInfo.getUnitADCacheTime(), unitGroupInfo, adTrackingInfo);
        cacheCountdownTimer.setCacheAdapter(mLocalMap);
        mCacheCountdownTimer = cacheCountdownTimer;
        mCacheCountdownTimer.start();
    }

    public void setOpenSetting(ATNativeOpenSetting setting, String unityId) {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mApplicationContext).getPlaceStrategyByAppIdAndPlaceId(unityId);
        if (placeStrategy != null) {
            setting.isAutoRefresh = placeStrategy.getAutoRefresh() == 1;
            setting.autoRefreshTime = placeStrategy.getAutoRefreshTime();
        }
    }

}
