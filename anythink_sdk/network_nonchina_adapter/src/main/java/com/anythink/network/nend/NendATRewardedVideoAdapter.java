/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.nend;

import android.app.Activity;
import android.content.Context;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import net.nend.android.NendAdRewardItem;
import net.nend.android.NendAdRewardedActionListener;
import net.nend.android.NendAdRewardedVideo;
import net.nend.android.NendAdVideo;
import net.nend.android.NendAdVideoPlayingState;
import net.nend.android.NendAdVideoPlayingStateListener;

import java.util.Map;

public class NendATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    String mApiKey;
    int mSpotId;
    NendAdRewardedVideo mNendAdRewardedVideo;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("api_key") && serverExtras.containsKey("spot_id")) {
            mApiKey = (String) serverExtras.get("api_key");
            mSpotId = Integer.parseInt((String) serverExtras.get("spot_id"));

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id or slot_id is empty!");
            }
            return;
        }

        mNendAdRewardedVideo = new NendAdRewardedVideo(context.getApplicationContext(), mSpotId, mApiKey);
        mNendAdRewardedVideo.setUserId(mUserId);
        mNendAdRewardedVideo.setActionListener(new NendAdRewardedActionListener() {
            @Override
            public void onRewarded(NendAdVideo nendAdVideo, NendAdRewardItem nendAdRewardItem) {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            @Override
            public void onLoaded(NendAdVideo nendAdVideo) {
                switch (mNendAdRewardedVideo.getType()) {

                    case NORMAL:
                        NendAdVideoPlayingState state = mNendAdRewardedVideo.playingState();
                        if (state != null) {
                            state.setPlayingStateListener(new NendAdVideoPlayingStateListener() {
                                @Override
                                public void onStarted(NendAdVideo nendAdVideo) {
                                    if (mImpressionListener != null) {
                                        mImpressionListener.onRewardedVideoAdPlayStart();
                                    }
                                }

                                @Override
                                public void onStopped(NendAdVideo nendAdVideo) {
                                    // 视频停止播放
                                }

                                @Override
                                public void onCompleted(NendAdVideo nendAdVideo) {
                                    if (mImpressionListener != null) {
                                        mImpressionListener.onRewardedVideoAdPlayEnd();
                                    }
                                }
                            });
                        }
                        break;
                    case PLAYABLE:
                    default:
                        break;
                }

                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onFailedToLoad(NendAdVideo nendAdVideo, int i) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(i + "", "");
                }
            }

            @Override
            public void onFailedToPlay(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayFailed("", "onFailedToPlay");
                }
            }

            @Override
            public void onShown(NendAdVideo nendAdVideo) {

            }

            @Override
            public void onClosed(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAdClicked(NendAdVideo nendAdVideo) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }

            @Override
            public void onInformationClicked(NendAdVideo nendAdVideo) {

            }
        });

        mNendAdRewardedVideo.loadAd();
    }

    @Override
    public void show(Activity activity) {
        if (activity != null) {
            if (mNendAdRewardedVideo != null && mNendAdRewardedVideo.isLoaded()) {
                mNendAdRewardedVideo.showAd((activity));
            }
        }
    }

    @Override
    public boolean isAdReady() {
        if (mNendAdRewardedVideo != null) {
            return mNendAdRewardedVideo.isLoaded();
        }
        return false;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public void destory() {
        if (mNendAdRewardedVideo != null) {
            mNendAdRewardedVideo.setActionListener(null);
            mNendAdRewardedVideo.releaseAd();
            mNendAdRewardedVideo = null;
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return NendATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(mSpotId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
