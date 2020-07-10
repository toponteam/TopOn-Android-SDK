package com.anythink.interstitial.business;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialEventListener;

public class InterstitialEventListener implements CustomInterstitialEventListener {

    ATInterstitialListener mListener;

    long impressionTime;

    public InterstitialEventListener(ATInterstitialListener listener) {
        mListener = listener;
    }

    @Override
    public void onInterstitialAdVideoStart(CustomInterstitialAdapter adapter) {
        if (adapter != null) {
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
            //发送视频开始统计
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_START_TYPE, adTrackingInfo);
            if (mListener != null) {
                mListener.onInterstitialAdVideoStart(ATAdInfo.fromAdapter(adapter));
            }

        }


    }

    @Override
    public void onInterstitialAdVideoEnd(CustomInterstitialAdapter adapter) {
        if (adapter != null) {
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
            //发送统计
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_CLOSE_TYPE, adTrackingInfo);
            if (mListener != null) {
                mListener.onInterstitialAdVideoEnd(ATAdInfo.fromAdapter(adapter));
            }
        }
    }

    @Override
    public void onInterstitialAdVideoError(CustomInterstitialAdapter adapter, final AdError errorCode) {
        if (adapter != null) {
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
            //发送统计

            AgentEventManager.rewardedVideoPlayFail(adTrackingInfo, errorCode);

        }
        if (mListener != null) {
            mListener.onInterstitialAdVideoError(errorCode);
        }


    }

    @Override
    public void onInterstitialAdClose(CustomInterstitialAdapter adapter) {
        if (adapter != null) {
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();

            adapter.log(Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");

            if (impressionTime != 0) {
                AgentEventManager.onAdImpressionTimeAgent(adTrackingInfo, false, impressionTime, System.currentTimeMillis());
            }

            AgentEventManager.onAdCloseAgent(adTrackingInfo, false);

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

        impressionTime = System.currentTimeMillis();
        if (adapter != null) {
            /**日志输出**/
            adapter.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

            //发送展示统计
            AdTrackingInfo adTrackingInfo = adapter.getTrackingInfo();
            long timestamp = System.currentTimeMillis();
            adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo, timestamp);

        }
        if (mListener != null) {
            mListener.onInterstitialAdShow(ATAdInfo.fromAdapter(adapter));
        }
    }
}
