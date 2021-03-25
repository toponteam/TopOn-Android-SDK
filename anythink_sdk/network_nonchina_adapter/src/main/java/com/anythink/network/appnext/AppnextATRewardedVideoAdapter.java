/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.appnext;

import android.app.Activity;
import android.content.Context;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
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
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("placement_id")) {
            mPlacementId = (String) serverExtras.get("placement_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "placement_id is empty!");
            }
            return;
        }

        AppnextATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras);

        mRewardedVideo = new RewardedVideo(context.getApplicationContext(), mPlacementId);
        mRewardedVideo.setRewardsUserId(mUserId);
        // Get callback for ad loaded
        mRewardedVideo.setOnAdLoadedCallback(new OnAdLoaded() {
            @Override
            public void adLoaded(String s, AppnextAdCreativeType appnextAdCreativeType) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }

            }
        });
        // Get callback for ad error
        mRewardedVideo.setOnAdErrorCallback(new OnAdError() {
            @Override
            public void adError(String error) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", error);
                }
            }
        });


        // Get callback for ad opened
        mRewardedVideo.setOnAdOpenedCallback(new OnAdOpened() {
            @Override
            public void adOpened() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }

            }
        });
        // Get callback for ad clicked
        mRewardedVideo.setOnAdClickedCallback(new OnAdClicked() {
            @Override
            public void adClicked() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }
        });

        // Get callback for ad closed
        mRewardedVideo.setOnAdClosedCallback(new OnAdClosed() {
            @Override
            public void onAdClosed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }
        });


        // Get callback when the user saw the video until the end (video ended)
        mRewardedVideo.setOnVideoEndedCallback(new OnVideoEnded() {
            @Override
            public void videoEnded() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
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
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AppnextATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        if (mRewardedVideo != null) {
            mRewardedVideo.showAd();
        }
    }

    @Override
    public void destory() {
        if (mRewardedVideo != null) {
            mRewardedVideo.destroy();
            mRewardedVideo = null;
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return AppnextATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }
}
