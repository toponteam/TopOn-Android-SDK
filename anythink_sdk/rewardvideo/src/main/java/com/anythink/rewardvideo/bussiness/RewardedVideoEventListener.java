package com.anythink.rewardvideo.bussiness;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardedVideoEventListener;

public class RewardedVideoEventListener implements CustomRewardedVideoEventListener {
    private ATRewardVideoListener mCallbackListener;
    long impressionTime;

    boolean isReward;

    public RewardedVideoEventListener(ATRewardVideoListener rewardVideoListener) {
        impressionTime = 0;
        mCallbackListener = rewardVideoListener;
    }

    @Override
    public void onRewardedVideoAdPlayStart(CustomRewardVideoAdapter customRewardVideoAd) {

        impressionTime = System.currentTimeMillis();
        if (customRewardVideoAd != null) {
            AdTrackingInfo adTrackingInfo = customRewardVideoAd.getTrackingInfo();
            long timestamp = System.currentTimeMillis();
            adTrackingInfo.setmShowId(CommonSDKUtil.creatImpressionId(adTrackingInfo.getmRequestId(), adTrackingInfo.getmUnitGroupUnitId(), timestamp));

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_START_TYPE, adTrackingInfo);

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo, timestamp);

            customRewardVideoAd.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");


        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdPlayStart(ATAdInfo.fromAdapter(customRewardVideoAd));
        }


    }

    @Override
    public void onRewardedVideoAdPlayEnd(CustomRewardVideoAdapter customRewardVideoAd) {

        if (customRewardVideoAd != null) {
            AdTrackingInfo adTrackingInfo = customRewardVideoAd.getTrackingInfo();

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_RV_CLOSE_TYPE, adTrackingInfo);
        }

        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdPlayEnd(ATAdInfo.fromAdapter(customRewardVideoAd));
        }


    }

    @Override
    public void onRewardedVideoAdPlayFailed(CustomRewardVideoAdapter customRewardVideoAd, final AdError errorCode) {
        if (customRewardVideoAd != null) {
            AdTrackingInfo adTrackingInfo = customRewardVideoAd.getTrackingInfo();


            customRewardVideoAd.log(Const.LOGKEY.IMPRESSION, Const.LOGKEY.FAIL, errorCode.printStackTrace());

            AgentEventManager.rewardedVideoPlayFail(adTrackingInfo, errorCode);
        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdPlayFailed(errorCode, ATAdInfo.fromAdapter(customRewardVideoAd));
        }

    }

    @Override
    public void onRewardedVideoAdClosed(CustomRewardVideoAdapter customRewardVideoAd) {
        if (customRewardVideoAd != null) {
            AdTrackingInfo adTrackingInfo = customRewardVideoAd.getTrackingInfo();


            customRewardVideoAd.log(Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");

            if (impressionTime != 0) {
                AgentEventManager.onAdImpressionTimeAgent(adTrackingInfo, isReward, impressionTime, System.currentTimeMillis());
            }

            AgentEventManager.onAdCloseAgent(adTrackingInfo, isReward);

        }

        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdClosed(ATAdInfo.fromAdapter(customRewardVideoAd));
        }


    }

    @Override
    public void onRewardedVideoAdPlayClicked(CustomRewardVideoAdapter customRewardVideoAd) {

        if (customRewardVideoAd != null) {
            AdTrackingInfo adTrackingInfo = customRewardVideoAd.getTrackingInfo();

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);

            customRewardVideoAd.log(Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");


        }
        if (mCallbackListener != null) {
            mCallbackListener.onRewardedVideoAdPlayClicked(ATAdInfo.fromAdapter(customRewardVideoAd));
        }

    }

    @Override
    public void onReward(CustomRewardVideoAdapter customRewardVideoAd) {
        isReward = true;
        if (mCallbackListener != null) {
            mCallbackListener.onReward(ATAdInfo.fromAdapter(customRewardVideoAd));
        }
    }
}
