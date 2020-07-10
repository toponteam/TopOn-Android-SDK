package com.anythink.banner.business;

import android.content.Context;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.business.utils.CustomBannerAdapterParser;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.List;
import java.util.Map;

/**
 * Mediation Manager
 */
public class MediationGroupManager extends CommonMediationManager {

    private ATBannerView mBannerView;

    private CustomBannerListener mCustomBannerListener = new CustomBannerListener() {
        @Override
        public void onBannerAdLoaded(final CustomBannerAdapter customBannerAd) {
            onAdLoaded(customBannerAd, null);

        }

        @Override
        public void onBannerAdLoadFail(CustomBannerAdapter adapter, final AdError adError) {
            onAdError(adapter, adError);
        }


        @Override
        public void onBannerAdClose(CustomBannerAdapter customBannerAd) {
            if (customBannerAd != null) {
                if (mCallbackListener != null) {
                    mCallbackListener.onBannerClose(mIsRefresh, customBannerAd);
                }
                AdTrackingInfo adTrackingInfo = customBannerAd.getTrackingInfo();

                /**Debug log**/
                customBannerAd.log(Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");

                if (adTrackingInfo != null) {
                    AgentEventManager.onAdCloseAgent(adTrackingInfo, false);
                }

            }


        }

        @Override
        public void onBannerAdShow(CustomBannerAdapter customBannerAd) {
        }

        @Override
        public void onBannerAdClicked(CustomBannerAdapter customBannerAd) {

            if (customBannerAd != null) {
                AdTrackingInfo adTrackingInfo = customBannerAd.getTrackingInfo();
                //Ad Tracking
                AdTrackingManager.getInstance(mApplcationContext).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);

                /**Debug log**/
                customBannerAd.log(Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");

                if (mCallbackListener != null) {
                    mCallbackListener.onBannerClicked(mIsRefresh, customBannerAd);
                }
            }

        }
    };

    protected MediationGroupManager(Context context) {
        super(context);
    }


    InnerBannerListener mCallbackListener;

    public void setCallbackListener(InnerBannerListener listener) {
        mCallbackListener = listener;
    }


    protected void loadBannerAd(ATBannerView bannerView, String placementId, String requestid, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> list) {
        mBannerView = bannerView;
        super.loadAd(placementId, requestid, placeStrategy, list);
    }

    @Override
    public void onDevelopLoaded() {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallbackListener != null) {
                    mCallbackListener.onBannerLoaded(mIsRefresh);
                }
            }
        });
    }

    @Override
    public void onDeveloLoadFail(final AdError adError) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallbackListener != null) {
                    mCallbackListener.onBannerFailed(mIsRefresh, adError);
                }
            }
        });
    }

    @Override
    public void startLoadAd(AnyThinkBaseAdapter baseAdapter, PlaceStrategy.UnitGroupInfo unitGroupInfo, Map<String, Object> serviceExtras) {
        if (baseAdapter instanceof CustomBannerAdapter) {
            CustomBannerAdapterParser.loadBannerAd(mBannerView, mActivityRef.get(), (CustomBannerAdapter) baseAdapter, unitGroupInfo, serviceExtras, null, mCustomBannerListener);
        }

    }


}
