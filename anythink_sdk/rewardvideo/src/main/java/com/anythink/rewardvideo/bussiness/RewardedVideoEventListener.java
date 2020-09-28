package com.anythink.rewardvideo.bussiness;

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
import com.anythink.core.common.MonitoringPlatformManager;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardedVideoEventListener;

public class RewardedVideoEventListener implements CustomRewardedVideoEventListener {
    private ATRewardVideoListener mCallbackListener;
    private CustomRewardVideoAdapter mRewardVideoAdapter;
    long impressionTime;

    boolean isReward;

    public RewardedVideoEventListener(CustomRewardVideoAdapter rewardVideoAdapter, ATRewardVideoListener rewardVideoListener) {
        impressionTime = 0;
        mCallbackListener = rewardVideoListener;
        mRewardVideoAdapter = rewardVideoAdapter;
    }

    @Override
    public void onRewardedVideoAdPlayStart() {

        impressionTime = System.currentTimeMillis();
        if (mRewardVideoAdapter != null) {
            AdTrackingInfo adTrackingInfo = mRewardVideoAdapter.getTrackingInfo();
            long timestamp = System.currentTimeMillis();
            adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_START_TYPE, adTrackingInfo);

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo, timestamp);

            mRewardVideoAdapter.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");


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


            mRewardVideoAdapter.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.FAIL, adError.printStackTrace());

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


            mRewardVideoAdapter.log(Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");

            if (impressionTime != 0) {
                AgentEventManager.onAdImpressionTimeAgent(adTrackingInfo, isReward, impressionTime, System.currentTimeMillis());
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

            mRewardVideoAdapter.log(Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");


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
}
