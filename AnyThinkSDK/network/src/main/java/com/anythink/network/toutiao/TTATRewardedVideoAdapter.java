package com.anythink.network.toutiao;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;

import java.util.Map;

public class TTATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private final String TAG = getClass().getSimpleName();

    String slotId = "";
    private TTRewardVideoAd mttRewardVideoAd;

    //TT Ad load listener
    TTAdNative.RewardVideoAdListener ttRewardAdListener = new TTAdNative.RewardVideoAdListener() {
        @Override
        public void onError(int code, String message) {
            log(TAG, "onError: code :" + code + "--message:" + message);
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(TTATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(code), message));
            }
        }

        //Callback of cached video file resources to local after video ad loading
        @Override
        public void onRewardVideoCached() {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdLoaded(TTATRewardedVideoAdapter.this);
            }
        }

        //Video creatives are loaded, such as title, video url, etc., excluding video files
        @Override
        public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
            mttRewardVideoAd = ad;
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdDataLoaded(TTATRewardedVideoAdapter.this);
            }
        }
    };

    //TT Advertising event listener
    TTRewardVideoAd.RewardAdInteractionListener interactionListener = new TTRewardVideoAd.RewardAdInteractionListener() {

        @Override
        public void onAdShow() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayStart(TTATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onAdVideoBarClick() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayClicked(TTATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onAdClose() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdClosed(TTATRewardedVideoAdapter.this);

            }
        }

        @Override
        public void onVideoComplete() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayEnd(TTATRewardedVideoAdapter.this);
            }

            if (mImpressionListener != null) {
                mImpressionListener.onReward(TTATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {

        }

        @Override
        public void onSkippedVideo() {

        }

        public void onVideoError() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayFailed(TTATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", "Callback VideoError"));
            }
        }
    };

    @Override
    public void loadRewardVideoAd(final Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, final CustomRewardVideoListener customRewardVideoListener) {

        mLoadResultListener = customRewardVideoListener;

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        String appId = (String) serverExtras.get("app_id");
        slotId = (String) serverExtras.get("slot_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(slotId)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or slot_id is empty!"));
            }
            return;
        }


        TTRewardedVideoSetting rewardedVideoSetting = null;
        if (mediationSetting != null && mediationSetting instanceof TTRewardedVideoSetting) {
            rewardedVideoSetting = (TTRewardedVideoSetting) mediationSetting;
        }


        final TTRewardedVideoSetting finalRewardedVideoSetting = rewardedVideoSetting;
        TTATInitManager.getInstance().initSDK(activity, serverExtras, new TTATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                startLoad(activity, finalRewardedVideoSetting);
            }
        });
    }

    private void startLoad(Context activity, TTRewardedVideoSetting rewardedVideoSetting) {
        TTAdManager ttAdManager = TTAdSdk.getAdManager();

        TTAdNative mTTAdNative = ttAdManager.createAdNative(activity);//baseContext is recommended for activity
        AdSlot.Builder adSlotBuilder = new AdSlot.Builder().setCodeId(slotId);
        int width = activity.getResources().getDisplayMetrics().widthPixels;
        int height = activity.getResources().getDisplayMetrics().heightPixels;
        adSlotBuilder.setImageAcceptedSize(width, height); //must be set

        if (rewardedVideoSetting != null) {
            adSlotBuilder = adSlotBuilder.setSupportDeepLink(rewardedVideoSetting.getSoupportDeepLink());
            if (rewardedVideoSetting.getVideoOrientation() == 1) {
                adSlotBuilder.setOrientation(TTAdConstant.VERTICAL);
            } else if (rewardedVideoSetting.getVideoOrientation() == 2) {
                adSlotBuilder.setOrientation(TTAdConstant.HORIZONTAL);
            }
        }
        if (!TextUtils.isEmpty(mUserId)) {
            adSlotBuilder.setUserID(mUserId);
        }

        adSlotBuilder.setAdCount(1);

        AdSlot adSlot = adSlotBuilder.build();
        mTTAdNative.loadRewardVideoAd(adSlot, ttRewardAdListener);
    }

    @Override
    public boolean isAdReady() {
        return mttRewardVideoAd != null;
    }

    @Override
    public void show(Activity activity) {
        if (mttRewardVideoAd != null) {
            mttRewardVideoAd.setRewardAdInteractionListener(interactionListener);
            mttRewardVideoAd.showRewardVideoAd(activity);
        }

    }

    @Override
    public void clean() {
        mttRewardVideoAd = null;
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public String getSDKVersion() {
        return TTATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return TTATInitManager.getInstance().getNetworkName();
    }
}
