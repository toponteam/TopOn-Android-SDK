package com.anythink.rewardvideo.bussiness;

import android.app.Activity;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.common.AdCacheManager;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ad Request Manager
 */

public class AdLoadManager extends CommonAdManager {


    public static final String TAG = AdLoadManager.class.getSimpleName();

    String mUserId;
    String mCustomData;


    public static AdLoadManager getInstance(Activity context, String placementId) {

        CommonAdManager adLoadManager = CommonAdManager.getInstance(placementId);
        if (adLoadManager == null || !(adLoadManager instanceof AdLoadManager)) {
            adLoadManager = new AdLoadManager(context, placementId);
            CommonAdManager.addAdManager(placementId, adLoadManager);
        }
        adLoadManager.refreshContext(context);
        return (AdLoadManager) adLoadManager;
    }


    private AdLoadManager(Activity context, String placementId) {
        super(context, placementId);
        mSettings = new HashMap<>();
    }

    public void show(final Activity activity, final String scenario, RewardedVideoEventListener listener) {
        AdCacheInfo adCacheInfo = isAdReady(activity, true);
        if (adCacheInfo == null) {
            AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "No Cache.");

            if (listener != null) {
                listener.onRewardedVideoAdPlayFailed(null, adError);
            }


            return;
        }


        if (adCacheInfo != null && adCacheInfo.getBaseAdapter() instanceof CustomRewardVideoAdapter) {
            cancelReturnCache(adCacheInfo);
            /**
             * Remove CountDown
             */
            cancelCountdown();

            AdCacheManager.getInstance().saveShowTime(mApplicationContext, adCacheInfo);
            final CustomRewardVideoAdapter customRewardVideoAdapter = ((CustomRewardVideoAdapter) adCacheInfo.getBaseAdapter());
            customRewardVideoAdapter.refreshActivityContext(activity);

            AdTrackingInfo adTrackingInfo = adCacheInfo.getBaseAdapter().getTrackingInfo();
            if (adTrackingInfo != null) {
                adTrackingInfo.setCurrentRequestId(mRequestId);
                adTrackingInfo.setmScenario(scenario);
            }

            AdTrackingManager.getInstance(mApplicationContext).addAdTrackingInfo(TrackingV2Loader.AD_SDK_SHOW_TYPE, adTrackingInfo);

            customRewardVideoAdapter.setUserId(mUserId);
            customRewardVideoAdapter.setAdImpressionListener(listener);

            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    customRewardVideoAdapter.show(activity);
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
     * Ad Request
     *
     * @param listener
     */
    public void startLoadAd(final Activity activity, final boolean isAutoRefresh, final Map<String, String> customExtraMap, final ATRewardVideoListener listener) {

        loadStragety(mApplicationContext, Const.FORMAT.REWARDEDVIDEO_FORMAT, mPlacementId, isAutoRefresh, customExtraMap, new CommonAdManager.PlacementCallback() {
            @Override
            public void onSuccess(String placementId, String requestId, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> unitGroupInfoList) {
                MediationGroupManager mediaionGroupManager = new MediationGroupManager(activity);
                mediaionGroupManager.setUserData(mUserId, mCustomData);
                mediaionGroupManager.setCallbackListener(listener);
                mediaionGroupManager.setNetworkSettingMap(mSettings);
                mediaionGroupManager.setRefresh(isAutoRefresh);
                mediaionGroupManager.loadRewardVideoAd(mPlacementId, requestId, placeStrategy, unitGroupInfoList);
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
                    listener.onRewardedVideoAdLoaded();
                }
            }

            @Override
            public void onLoadError(String placementId, String requestId, AdError adError) {
                if (listener != null) {
                    listener.onRewardedVideoAdFailed(adError);
                }
            }

        });
    }


    @Override
    public void startCountdown(PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        if (mActivityRef.get() instanceof Activity) {
            ATMediationSetting setting = null;
            if (mSettings != null) {
                setting = mSettings.get(unitGroupInfo.networkType);
            }
            CacheCountdownTimer cacheCountdownTimer = new CacheCountdownTimer(unitGroupInfo.getUnitADCacheTime(), unitGroupInfo.getUnitADCacheTime(), unitGroupInfo, adTrackingInfo);
            cacheCountdownTimer.setExtraInfo(setting);
            mCacheCountdownTimer = cacheCountdownTimer;
            mCacheCountdownTimer.start();
        }
    }


    public void setUserData(String userId, String customData) {
        mUserId = userId;
        mCustomData = customData;
    }


}
