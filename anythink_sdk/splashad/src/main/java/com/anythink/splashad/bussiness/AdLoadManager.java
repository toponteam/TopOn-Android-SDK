package com.anythink.splashad.bussiness;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.splashad.api.ATSplashAdListener;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.List;
import java.util.Map;

/**
 * Ad Request Manager
 */

public class AdLoadManager extends CommonAdManager {


    public AdLoadManager(Context context, String placementId) {
        super(context, placementId);
    }



    public static AdLoadManager getInstance(Activity context, String placementId) {

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
    public void startLoadAd(final ViewGroup container, final View skipView, final ATSplashAdListener listener) {
        loadStragety(mApplicationContext, Const.FORMAT.SPLASH_FORMAT, mPlacementId, false, new PlacementCallback() {
            @Override
            public void onSuccess(String placementId, String requestId, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> unitGroupInfoList) {
                MediationGroupManager mediationManager = new MediationGroupManager((Activity) mActivityRef.get());
                mediationManager.setCallbackListener(listener);
                mediationManager.loadSplashAd(container, skipView, mPlacementId, requestId, placeStrategy, unitGroupInfoList);

                mCurrentManager = mediationManager;
            }

            @Override
            public void onAdLoaded(String placementId, String requestId) {
                if (listener != null) {
                    listener.onAdLoaded();
                }
            }

            @Override
            public void onLoadError(String placementId, String requestId, AdError adError) {
                if (listener != null) {
                    listener.onNoAdError(adError);
                }
            }
        });
    }

    @Override
    public void startCountdown(PlaceStrategy.UnitGroupInfo unitGroupInfo, AdTrackingInfo adTrackingInfo) {
        /**Splash no need to do this**/
    }

    public void release() {
        if (mCurrentManager != null) {
            mCurrentManager.release();
        }
    }

}
