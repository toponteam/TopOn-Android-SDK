/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.interstitial.business;

import android.os.SystemClock;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.interstitial.api.ATInterstitialExListener;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialEventListener;

public class InterstitialEventListener implements CustomInterstitialEventListener {

    ATInterstitialListener mListener;
    CustomInterstitialAdapter mAdapter;
    long impressionTime;
    long impressionRealTime;

    public InterstitialEventListener(CustomInterstitialAdapter adapter, ATInterstitialListener listener) {
        mListener = listener;
        mAdapter = adapter;
    }

    @Override
    public void onInterstitialAdVideoStart() {
        if (mAdapter != null) {
            AdTrackingInfo adTrackingInfo = mAdapter.getTrackingInfo();
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_START_TYPE, adTrackingInfo);
            if (mListener != null) {
                mListener.onInterstitialAdVideoStart(ATAdInfo.fromAdapter(mAdapter));
            }
        }
    }

    @Override
    public void onInterstitialAdVideoEnd() {
        if (mAdapter != null) {
            AdTrackingInfo adTrackingInfo = mAdapter.getTrackingInfo();
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_CLOSE_TYPE, adTrackingInfo);
            if (mListener != null) {
                mListener.onInterstitialAdVideoEnd(ATAdInfo.fromAdapter(mAdapter));
            }
        }
    }

    @Override
    public void onInterstitialAdVideoError(String errorCode, String errorMsg) {
        AdError adError = ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, errorCode, errorMsg);
        if (mAdapter != null) {
            AdTrackingInfo adTrackingInfo = mAdapter.getTrackingInfo();
            AgentEventManager.rewardedVideoPlayFail(adTrackingInfo, adError);

        }
        if (mListener != null) {
            mListener.onInterstitialAdVideoError(adError);
        }
    }

    @Override
    public void onInterstitialAdClose() {
        if (mAdapter != null) {
            AdTrackingInfo adTrackingInfo = mAdapter.getTrackingInfo();

            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");

            if (impressionTime != 0) {
                AgentEventManager.onAdImpressionTimeAgent(adTrackingInfo, false, impressionTime, System.currentTimeMillis(), SystemClock.elapsedRealtime() - impressionRealTime);
            }

            AgentEventManager.onAdCloseAgent(adTrackingInfo, false);

            try {
                mAdapter.clearImpressionListener();
                mAdapter.destory();
            } catch (Throwable e) {

            }

            if (mListener != null) {
                mListener.onInterstitialAdClose(ATAdInfo.fromAdapter(mAdapter));
            }

        }


    }

    @Override
    public void onInterstitialAdClicked() {
        if (mAdapter != null) {
            AdTrackingInfo adTrackingInfo = mAdapter.getTrackingInfo();
            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");
            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);

        }
        if (mListener != null) {
            mListener.onInterstitialAdClicked(ATAdInfo.fromAdapter(mAdapter));
        }
    }

    @Override
    public void onInterstitialAdShow() {

        impressionTime = System.currentTimeMillis();
        impressionRealTime = SystemClock.elapsedRealtime();
        if (mAdapter != null) {
            AdTrackingInfo adTrackingInfo = mAdapter.getTrackingInfo();
            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo);

        }
        if (mListener != null) {
            mListener.onInterstitialAdShow(ATAdInfo.fromAdapter(mAdapter));
        }
    }

    @Override
    public void onDeeplinkCallback(boolean isSuccess) {
        if (mListener != null && mListener instanceof ATInterstitialExListener) {
            ((ATInterstitialExListener) mListener).onDeeplinkCallback(ATAdInfo.fromAdapter(mAdapter), isSuccess);
        }
    }
}
