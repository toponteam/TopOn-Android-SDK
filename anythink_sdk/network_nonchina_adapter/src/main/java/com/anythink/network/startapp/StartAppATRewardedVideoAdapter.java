package com.anythink.network.startapp;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.VideoListener;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.model.AdPreferences;

import java.util.Map;

public class StartAppATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    StartAppAd startAppAd;
    String adTag = "";

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

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


        if (TextUtils.isEmpty(appId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id could not be null.");
            }
            return;
        }

        StartAppATInitManager.getInstance().initSDK(context, serverExtras);
        AdPreferences adPreferences = new AdPreferences();
        if (!TextUtils.isEmpty(adTag)) {
            adPreferences.setAdTag(adTag);
        }

        startAppAd = new StartAppAd(context);
        startAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, adPreferences, new AdEventListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onFailedToReceiveAd(Ad ad) {
                if (mLoadListener != null) {
                    if (ad != null) {
                        mLoadListener.onAdLoadError("", ad.getErrorMessage());
                    } else {
                        mLoadListener.onAdLoadError("", "StartApp has not error msg.");
                    }
                }
            }
        });

    }


    @Override
    public void show(Activity activity) {
        if (startAppAd != null && startAppAd.isReady()) {
            startAppAd.setVideoListener(new VideoListener() {
                @Override
                public void onVideoCompleted() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayEnd();
                    }

                    if (mImpressionListener != null) {
                        mImpressionListener.onReward();
                    }

                }
            });
            AdDisplayListener adDisplayListener = new AdDisplayListener() {
                @Override
                public void adHidden(Ad ad) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdClosed();
                    }
                }

                @Override
                public void adDisplayed(Ad ad) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayStart();
                    }
                }

                @Override
                public void adClicked(Ad ad) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayClicked();
                    }
                }

                @Override
                public void adNotDisplayed(Ad ad) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayFailed("", ad.getErrorMessage());
                    }
                }
            };

            if (!TextUtils.isEmpty(adTag)) {
                startAppAd.showAd(adTag, adDisplayListener);
            } else {
                startAppAd.showAd(adDisplayListener);
            }

        }
    }


    @Override
    public boolean isAdReady() {
        return startAppAd != null && startAppAd.isReady();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public String getNetworkSDKVersion() {
        return StartAppATConst.getSDKVersion();
    }

    @Override
    public void destory() {
        if (startAppAd != null) {
            startAppAd.setVideoListener(null);
            startAppAd = null;
        }
    }

    @Override
    public String getNetworkName() {
        return StartAppATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return adTag;
    }
}
