package com.anythink.network.uniplay;

import android.app.Activity;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.uniplay.adsdk.VideoAd;
import com.uniplay.adsdk.VideoAdListener;

import java.util.Map;

public class UniplayATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private final String TAG = getClass().getSimpleName();

    String appId = "";
    private VideoAd mVideoAd;

    // Ad load listener
    VideoAdListener mAdListener = new VideoAdListener() {

        @Override
        public void onVideoAdReady() {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdLoaded(UniplayATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onVideoAdStart() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayStart(UniplayATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onVideoAdProgress(int i, int i1) {
            log(TAG, "onVideoAdProgress:" + i);
        }

        @Override
        public void onVideoAdFailed(String s) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(UniplayATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
            }
        }

        @Override
        public void onVideoAdComplete() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayEnd(UniplayATRewardedVideoAdapter.this);
            }

            if (mImpressionListener != null) {
                mImpressionListener.onReward(UniplayATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onVideoAdClose() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdClosed(UniplayATRewardedVideoAdapter.this);
            }
        }
    };

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, final CustomRewardVideoListener customRewardVideoListener) {

        mLoadResultListener = customRewardVideoListener;

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("app_id")) {
            appId = (String) serverExtras.get("app_id");

        } else {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id is empty!"));
            }
            return;
        }

        mVideoAd = VideoAd.getInstance().init(activity, appId, mAdListener);
        mVideoAd.loadVideoAd();

    }

    @Override
    public boolean isAdReady() {
        if (mVideoAd != null) {
            return mVideoAd.isVideoReady();
        }
        return false;

    }

    @Override
    public void show(Activity activity) {
        if (mVideoAd != null) {
            mVideoAd.playVideoAd();
        }

    }

    @Override
    public void clean() {
        mVideoAd = null;
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return UniplayATInitManager.getInstance().getNetworkName();
    }
}
