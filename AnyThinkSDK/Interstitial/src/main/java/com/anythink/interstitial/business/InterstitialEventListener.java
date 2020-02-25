package com.anythink.interstitial.business;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialEventListener;

public class InterstitialEventListener implements CustomInterstitialEventListener {

    ATInterstitialListener mListener;

    public InterstitialEventListener(ATInterstitialListener listener) {
        mListener = listener;
    }

    @Override
    public void onInterstitialAdVideoStart(CustomInterstitialAdapter adapter) {
        if (adapter != null) {
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
            //发送视频开始统计
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_START_TYPE, adTrackingInfo);

        }
        if (mListener != null) {
            mListener.onInterstitialAdVideoStart();
        }

    }

    @Override
    public void onInterstitialAdVideoEnd(CustomInterstitialAdapter adapter) {
        if (adapter != null) {
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
            //发送统计
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_CLOSE_TYPE, adTrackingInfo);

        }
        if (mListener != null) {
            mListener.onInterstitialAdVideoEnd();
        }


    }

    @Override
    public void onInterstitialAdVideoError(CustomInterstitialAdapter adapter, final AdError errorCode) {
        if (adapter != null) {
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
            //发送统计

            AgentEventManager.rewardedVideoPlayFail(adTrackingInfo.getmRequestId(), adTrackingInfo.getmPlacementId(), adTrackingInfo.getmPsid(), adTrackingInfo.getmSessionId()
                    , adTrackingInfo.getmGroupId(), adTrackingInfo.getmRefresh(), adTrackingInfo.getmNetworkType(), adTrackingInfo.getmUnitGroupUnitId()
                    , adTrackingInfo.getmLevel(), errorCode.getCode(), errorCode.getPlatformCode(), errorCode.getPlatformMSG());


        }
        if (mListener != null) {
            mListener.onInterstitialAdVideoError(errorCode);
        }


    }

    @Override
    public void onInterstitialAdClose(CustomInterstitialAdapter adapter) {
        if (adapter != null) {
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();

            /**日志输出**/
            adapter.log(Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");


            AgentEventManager.onAdCloseAgent(adTrackingInfo.getmRequestId(), adTrackingInfo.getmPlacementId(), adTrackingInfo.getmPsid()
                    , adTrackingInfo.getmSessionId(), adTrackingInfo.getmGroupId(), adTrackingInfo.getmRefresh()
                    , adTrackingInfo.getmNetworkType(), adTrackingInfo.getmUnitGroupUnitId()
                    , adTrackingInfo.getmLevel(), false, adTrackingInfo.getMyOfferShowType());

            if (mListener != null) {
                mListener.onInterstitialAdClose(ATAdInfo.fromAdapter(adapter));
            }

            adapter.clearImpressionListener();
        }


    }

    @Override
    public void onInterstitialAdClicked(CustomInterstitialAdapter adapter) {
        if (adapter != null) {
            adapter.log(Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
            //发送统计
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);

        }
        if (mListener != null) {
            mListener.onInterstitialAdClicked(ATAdInfo.fromAdapter(adapter));
        }
    }

    @Override
    public void onInterstitialAdShow(CustomInterstitialAdapter adapter) {

        if (adapter != null) {
            /**日志输出**/
            adapter.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

            //发送展示统计
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo);

        }
        if (mListener != null) {
            mListener.onInterstitialAdShow(ATAdInfo.fromAdapter(adapter));
        }
    }
}
