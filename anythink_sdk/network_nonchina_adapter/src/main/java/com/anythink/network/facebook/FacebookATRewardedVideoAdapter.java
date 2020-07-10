package com.anythink.network.facebook;

import android.app.Activity;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.RewardData;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */

public class FacebookATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private static final String TAG = FacebookATRewardedVideoAdapter.class.getSimpleName();

    FacebookRewardedVideoSetting mFacebookMediationSetting;

    RewardedVideoAd rewardedVideoAd;
    String mUnitid;

    String mPayload;

    /***
     * load ad
     */
    private void startLoad(final Activity activity) {

        final RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                // Rewarded video ad failed to load
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(FacebookATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, error.getErrorCode() + "", "" + error.getErrorMessage()));
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Rewarded video ad is loaded and ready to be displayed
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(FacebookATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Rewarded video ad clicked
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(FacebookATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Rewarded Video ad impression - the event will fire when the
                // video starts playing
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(FacebookATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onRewardedVideoCompleted() {
                // Rewarded Video View Complete - the video has been played to the end.
                // You can use this event to initialize your reward
                // Call method to give reward
                // giveReward();
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(FacebookATRewardedVideoAdapter.this);
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward(FacebookATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onRewardedVideoClosed() {
                // The Rewarded Video ad was closed - this can occur during the video
                // by closing the app, or closing the end card.
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed(FacebookATRewardedVideoAdapter.this);
                }

            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                rewardedVideoAd = new RewardedVideoAd(activity.getApplicationContext(), mUnitid);
                RewardedVideoAd.RewardedVideoAdLoadConfigBuilder adConfig = rewardedVideoAd
                        .buildLoadAdConfig()
                        .withAdListener(rewardedVideoAdListener)
                        .withFailOnCacheFailureEnabled(true)
                        .withRVChainEnabled(true);

                if (mFacebookMediationSetting != null) {
                    adConfig.withRewardData(new RewardData(mUserId, mFacebookMediationSetting.getRewardData()));
                } else {
                    adConfig.withRewardData(new RewardData(mUserId, ""));
                }

                if (!TextUtils.isEmpty(mPayload)) {
                    adConfig.withBid(mPayload);
                }
                rewardedVideoAd.loadAd(adConfig.build());
            }
        }).start();
    }

    @Override
    public void clean() {
        if (check()) {
            rewardedVideoAd.destroy();
        }
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }


    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;
        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof FacebookRewardedVideoSetting) {
            mFacebookMediationSetting = (FacebookRewardedVideoSetting) mediationSetting;

        }


        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "facebook serverExtras is empty."));
            }
            return;
        } else {


            if (serverExtras.containsKey("unit_id")) {
                mUnitid = (String) serverExtras.get("unit_id");
            } else {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "facebook sdkkey is empty."));
                }
                return;
            }
        }
        FacebookATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras);

        if (serverExtras.containsKey("payload")) {
            mPayload = serverExtras.get("payload").toString();
        }

        startLoad(activity);
    }

    @Override
    public boolean isAdReady() {
        if (rewardedVideoAd == null || !rewardedVideoAd.isAdLoaded()) {
            return false;
        }
        if (rewardedVideoAd.isAdInvalidated()) {
            return false;
        }
        return true;
    }

    @Override
    public void show(Activity activity) {
        if (check() && isAdReady()) {
            rewardedVideoAd.show();
        }

    }

    private boolean check() {
        if (rewardedVideoAd == null) {
            return false;
        }
        return true;
    }

    @Override
    public String getSDKVersion() {
        return FacebookATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return FacebookATInitManager.getInstance().getNetworkName();
    }
}