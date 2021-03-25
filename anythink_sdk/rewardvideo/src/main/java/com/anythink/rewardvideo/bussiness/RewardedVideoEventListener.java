/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.rewardvideo.bussiness;

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
import com.anythink.rewardvideo.api.ATRewardVideoExListener;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardedVideoEventListener;

public class RewardedVideoEventListener implements CustomRewardedVideoEventListener {
    private ATRewardVideoListener mCallbackListener;
    private CustomRewardVideoAdapter mRewardVideoAdapter;
    long impressionTime;
    long impressionRealTime;

    boolean isReward;

    public RewardedVideoEventListener(CustomRewardVideoAdapter rewardVideoAdapter, ATRewardVideoListener rewardVideoListener) {
        impressionTime = 0;
        mCallbackListener = rewardVideoListener;
        mRewardVideoAdapter = rewardVideoAdapter;
    }

    @Override
    public void onRewardedVideoAdPlayStart() {

        impressionTime = System.currentTimeMillis();
        impressionRealTime = SystemClock.elapsedRealtime();
        if (mRewardVideoAdapter != null) {
            AdTrackingInfo adTrackingInfo = mRewardVideoAdapter.getTrackingInfo();

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_START_TYPE, adTrackingInfo);

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo);

            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");


        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdPlayStart(ATAdInfo.fromAdapter(mRewardVideoAdapter));
        }


    }

    @Override
    public void onRewardedVideoAdPlayEnd() {

        if (mRewardVideoAdapter != null) {
            AdTrackingInfo adTrackingInfo = mRewardVideoAdapter.getTrackingInfo();

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_CLOSE_TYPE, adTrackingInfo);
        }

        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdPlayEnd(ATAdInfo.fromAdapter(mRewardVideoAdapter));
        }


    }

    @Override
    public void onRewardedVideoAdPlayFailed(String errorCode, String errorMsg) {
        AdError adError = ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, errorCode, errorMsg);
        if (mRewardVideoAdapter != null) {
            AdTrackingInfo adTrackingInfo = mRewardVideoAdapter.getTrackingInfo();


            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.IMPRESSION, Const.LOGKEY.FAIL, adError.printStackTrace());

            AgentEventManager.rewardedVideoPlayFail(adTrackingInfo, adError);
        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdPlayFailed(adError, ATAdInfo.fromAdapter(mRewardVideoAdapter));
        }

        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                mRewardVideoAdapter.clearImpressionListener();
                mRewardVideoAdapter.destory();
            }
        });

    }

    @Override
    public void onRewardedVideoAdClosed() {
        if (mRewardVideoAdapter != null) {
            AdTrackingInfo adTrackingInfo = mRewardVideoAdapter.getTrackingInfo();


            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");

            if (impressionTime != 0) {
                AgentEventManager.onAdImpressionTimeAgent(adTrackingInfo, isReward, impressionTime, System.currentTimeMillis(), SystemClock.elapsedRealtime() - impressionRealTime);
            }

            AgentEventManager.onAdCloseAgent(adTrackingInfo, isReward);

            if (isReward) {
                try {
                    mRewardVideoAdapter.clearImpressionListener();
                    mRewardVideoAdapter.destory();
                } catch (Throwable e) {

                }

            } else {
                SDKContext.getInstance().runOnMainThreadDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mRewardVideoAdapter.clearImpressionListener();
                            mRewardVideoAdapter.destory();
                        } catch (Throwable e) {

                        }
                    }
                }, 5000);
            }

            if (mCallbackListener != null) {
                mCallbackListener.onRewardedVideoAdClosed(ATAdInfo.fromAdapter(mRewardVideoAdapter));
            }
        }

    }

    @Override
    public void onRewardedVideoAdPlayClicked() {

        if (mRewardVideoAdapter != null) {
            AdTrackingInfo adTrackingInfo = mRewardVideoAdapter.getTrackingInfo();

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);

            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");


        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdPlayClicked(ATAdInfo.fromAdapter(mRewardVideoAdapter));
        }

    }

    @Override
    public void onReward() {
        isReward = true;
        if (mCallbackListener != null) {
            mCallbackListener.onReward(ATAdInfo.fromAdapter(mRewardVideoAdapter));
        }
    }

    @Override
    public void onDeeplinkCallback(boolean isSuccess) {
        if (mCallbackListener != null && mCallbackListener instanceof ATRewardVideoExListener) {
            ((ATRewardVideoExListener) mCallbackListener).onDeeplinkCallback(ATAdInfo.fromAdapter(mRewardVideoAdapter), isSuccess);
        }
    }
}
