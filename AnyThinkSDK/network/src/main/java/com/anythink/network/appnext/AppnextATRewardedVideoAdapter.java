package com.anythink.network.appnext;

import android.app.Activity;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.appnext.ads.fullscreen.RewardedVideo;
import com.appnext.core.AppnextAdCreativeType;
import com.appnext.core.callbacks.OnAdClicked;
import com.appnext.core.callbacks.OnAdClosed;
import com.appnext.core.callbacks.OnAdError;
import com.appnext.core.callbacks.OnAdLoaded;
import com.appnext.core.callbacks.OnAdOpened;
import com.appnext.core.callbacks.OnVideoEnded;

import java.util.Map;

public class AppnextATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    String mPlacementId;


    RewardedVideo mRewardedVideo;

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {

        mLoadResultListener = customRewardVideoListener;

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(AppnextATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("placement_id")) {
            mPlacementId = (String) serverExtras.get("placement_id");

        } else {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(AppnextATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "placement_id is empty!"));
            }
            return;
        }

        AppnextATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras);

        mRewardedVideo = new RewardedVideo(activity, mPlacementId);
        mRewardedVideo.setRewardsUserId(mUserId);
        // Get callback for ad loaded
        mRewardedVideo.setOnAdLoadedCallback(new OnAdLoaded() {
            @Override
            public void adLoaded(String s, AppnextAdCreativeType appnextAdCreativeType) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(AppnextATRewardedVideoAdapter.this);
                }

            }
        });
        // Get callback for ad error
        mRewardedVideo.setOnAdErrorCallback(new OnAdError() {
            @Override
            public void adError(String error) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(AppnextATRewardedVideoAdapter.this
                            , ErrorCode.getErrorCode(ErrorCode.noADError, "", error));
                }
            }
        });


        // Get callback for ad opened
        mRewardedVideo.setOnAdOpenedCallback(new OnAdOpened() {
            @Override
            public void adOpened() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(AppnextATRewardedVideoAdapter.this);
                }

            }
        });
        // Get callback for ad clicked
        mRewardedVideo.setOnAdClickedCallback(new OnAdClicked() {
            @Override
            public void adClicked() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(AppnextATRewardedVideoAdapter.this);
                }
            }
        });

        // Get callback for ad closed
        mRewardedVideo.setOnAdClosedCallback(new OnAdClosed() {
            @Override
            public void onAdClosed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed(AppnextATRewardedVideoAdapter.this);
                }
            }
        });


        // Get callback when the user saw the video until the end (video ended)
        mRewardedVideo.setOnVideoEndedCallback(new OnVideoEnded() {
            @Override
            public void videoEnded() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(AppnextATRewardedVideoAdapter.this);
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward(AppnextATRewardedVideoAdapter.this);
                }
            }
        });

        mRewardedVideo.loadAd();


    }

    @Override
    public boolean isAdReady() {
        if (mRewardedVideo != null) {
            return mRewardedVideo.isAdLoaded();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (mRewardedVideo != null) {
            mRewardedVideo.showAd();
        }
    }

    @Override
    public void clean() {
        if (mRewardedVideo != null) {
            mRewardedVideo.destroy();
        }
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
        return AppnextATInitManager.getInstance().getNetworkName();
    }
}
