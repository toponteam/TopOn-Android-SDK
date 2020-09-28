package com.anythink.network.uniplay;

import android.app.Activity;
import android.content.Context;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
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
            if (mLoadListener != null) {
                mLoadListener.onAdCacheLoaded();
            }
        }

        @Override
        public void onVideoAdStart() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayStart();
            }
        }

        @Override
        public void onVideoAdProgress(int i, int i1) {
        }

        @Override
        public void onVideoAdFailed(String s) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", s);
            }
        }

        @Override
        public void onVideoAdComplete() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayEnd();
            }

            if (mImpressionListener != null) {
                mImpressionListener.onReward();
            }
        }

        @Override
        public void onVideoAdClose() {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdClosed();
            }
        }
    };


    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        if (serverExtra.containsKey("app_id")) {
            appId = (String) serverExtra.get("app_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id is empty!");
            }
            return;
        }

        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mVideoAd = VideoAd.getInstance().init(context.getApplicationContext(), appId, mAdListener);
                    mVideoAd.loadVideoAd();
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
    public String getNetworkName() {
        return UniplayATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        if (mVideoAd != null) {
            mVideoAd.setOnVideoAdStateListener(null);
            mVideoAd = null;
        }
        mAdListener = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return appId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }
}
