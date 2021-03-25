/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.mopub;

import android.app.Activity;
import android.content.Context;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.util.Map;
import java.util.Set;


public class MopubATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private final String TAG = MopubATRewardedVideoAdapter.class.getSimpleName();

    String adUnitId;
    MoPubRewardedVideoListener mMoPubRewardedVideoListener;

    private void startLoad(Activity activity) {
        mMoPubRewardedVideoListener = new MoPubRewardedVideoListener() {
            @Override
            public void onRewardedVideoLoadSuccess(String adUnitId) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onRewardedVideoLoadFailure(String adUnitId, MoPubErrorCode errorCode) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(errorCode.getIntCode() + "", errorCode.toString());
                }
            }

            @Override
            public void onRewardedVideoStarted(String adUnitId) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onRewardedVideoPlaybackError(String adUnitId, MoPubErrorCode errorCode) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayFailed(errorCode.getIntCode() + "", errorCode.toString());
                }
            }

            @Override
            public void onRewardedVideoClicked(String adUnitId) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }

            @Override
            public void onRewardedVideoClosed(String adUnitId) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
                if (mActivityRef != null) {
                    MoPub.onStop(mActivityRef.get());
                    MoPub.onDestroy(mActivityRef.get());
                }
            }

            @Override
            public void onRewardedVideoCompleted(Set<String> adUnitIds, MoPubReward reward) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }
        };
        //RewardVideo needs initialization
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(adUnitId).build();
        MoPub.initializeSdk(activity, sdkConfiguration, null);

        MoPubRewardedVideos.setRewardedVideoListener(mMoPubRewardedVideoListener);
        MoPub.onCreate(activity);
        MoPub.onStart(activity);
        MoPubRewardedVideos.loadRewardedVideo(adUnitId, new MoPubRewardedVideoManager.RequestParameters(mUserData, "", null, mUserId));
    }

    @Override
    public void destory() {
    }


    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {
        if (serverExtras.containsKey("unitid")) {
            adUnitId = (String) serverExtras.get("unitid");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "unitid is empty!");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Mopub context must be activity.");
            }
            return;
        }

        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    MopubATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new MopubATInitManager.InitListener() {
                        @Override
                        public void initSuccess() {
                            try {
                                startLoad(((Activity) context));
                            } catch (Throwable e) {
                                if (mLoadListener != null) {
                                    mLoadListener.onAdLoadError("", e.getMessage());
                                }
                            }
                        }
                    });
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public boolean isAdReady() {
        return MoPubRewardedVideos.hasRewardedVideo(adUnitId);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return MopubATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            MoPubRewardedVideos.showRewardedVideo(adUnitId, mUserId);
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return MopubATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MopubATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return adUnitId;
    }

}