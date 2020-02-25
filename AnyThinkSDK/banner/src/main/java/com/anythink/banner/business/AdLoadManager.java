package com.anythink.banner.business;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.banner.api.ATBannerView;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.List;
import java.util.Map;

/**
 * Ad request manager
 */

public class AdLoadManager extends CommonAdManager {


    public static final String TAG = "Banner" + AdLoadManager.class.getSimpleName();


    private AdLoadManager(Context context, String placementId) {
        super(context, placementId);
    }


    public static AdLoadManager getInstance(Context context, String placementId) {

        CommonAdManager adLoadManager = CommonAdManager.getInstance(placementId);
        if (adLoadManager == null || !(adLoadManager instanceof AdLoadManager)) {
            adLoadManager = new AdLoadManager(context, placementId);
            CommonAdManager.addAdManager(placementId, adLoadManager);
        }
        adLoadManager.refreshContext(context);
        return (AdLoadManager) adLoadManager;
    }

    /**
     * Ad Request
     *
     * @param listener
     */
    public void startLoadAd(final ATBannerView bannerView, final boolean isRefresh, final Map<String, String> customExtraMap, final InnerBannerListener listener) {

        loadStragety(mApplicationContext, Const.FORMAT.BANNER_FORMAT, mPlacementId, isRefresh, customExtraMap, new PlacementCallback() {
            @Override
            public void onSuccess(String placementId, String requestId, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> unitGroupInfoList) {
                MediationGroupManager mediaionGroupManager = new MediationGroupManager(mActivityRef.get());
                mediaionGroupManager.setCallbackListener(listener);
                mediaionGroupManager.setRefresh(isRefresh);
                mediaionGroupManager.loadBannerAd(bannerView, mPlacementId, requestId, placeStrategy, unitGroupInfoList);
                mHistoryMediationManager.put(requestId, mediaionGroupManager);

                mCurrentManager = mediaionGroupManager;
            }

            @Override
            public void onAdLoaded(String placementId, String requestId) {
                if (listener != null) {
                    listener.onBannerLoaded(isRefresh);
                }
            }

            @Override
            public void onLoadError(String placementId, String requestId, AdError adError) {
                if (listener != null) {
                    listener.onBannerFailed(isRefresh, adError);
                }
            }


        });

    }


    @Override
    public void startCountdown(PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        /**banner no need to do thie**/
    }
}
