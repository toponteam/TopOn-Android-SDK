package com.anythink.network.startapp;

import android.app.Activity;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.startapp.android.publish.adsCommon.Ad;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.VideoListener;
import com.startapp.android.publish.adsCommon.adListeners.AdDisplayListener;
import com.startapp.android.publish.adsCommon.adListeners.AdEventListener;

import java.util.Map;

public class StartAppATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    StartAppAd startAppAd;
    String adTag = "";

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {

        String appId = "";

        if (serverExtras.containsKey("app_id")) {
            appId = serverExtras.get("app_id").toString();
        }
        if (serverExtras.containsKey("ad_tag")) {
            adTag = serverExtras.get("ad_tag").toString();
        }

        if (adTag == null) {
            adTag = "";
        }

        mLoadResultListener = customRewardVideoListener;

        if (TextUtils.isEmpty(appId)) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id could not be null.");
                mLoadResultListener.onRewardedVideoAdFailed(this, adError);
            }
            return;
        }

        StartAppATInitManager.getInstance().initSDK(activity, serverExtras);
        startAppAd = new StartAppAd(activity);
        startAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, new AdEventListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(StartAppATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onFailedToReceiveAd(Ad ad) {
                if (mLoadResultListener != null) {
                    if (ad != null) {
                        mLoadResultListener.onRewardedVideoAdFailed(StartAppATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", ad.getErrorMessage()));
                    } else {
                        mLoadResultListener.onRewardedVideoAdFailed(StartAppATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "StartApp has not error msg."));
                    }
                }
            }
        });

    }


    @Override
    public void show(Activity activity) {
        if (startAppAd.isReady()) {
            startAppAd.setVideoListener(new VideoListener() {
                @Override
                public void onVideoCompleted() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayEnd(StartAppATRewardedVideoAdapter.this);
                    }

                    if (mImpressionListener != null) {
                        mImpressionListener.onReward(StartAppATRewardedVideoAdapter.this);
                    }

                }
            });
            startAppAd.showAd(adTag, new AdDisplayListener() {
                @Override
                public void adHidden(Ad ad) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdClosed(StartAppATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void adDisplayed(Ad ad) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayStart(StartAppATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void adClicked(Ad ad) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayClicked(StartAppATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void adNotDisplayed(Ad ad) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayFailed(StartAppATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", ad.getErrorMessage()));
                    }
                }
            });
        }
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public boolean isAdReady() {
        return startAppAd != null && startAppAd.isReady();
    }

    @Override
    public String getSDKVersion() {
        return StartAppATConst.getSDKVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return StartAppATInitManager.getInstance().getNetworkName();
    }
}
