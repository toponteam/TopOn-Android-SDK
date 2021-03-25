/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.baidu;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.baidu.mobads.rewardvideo.RewardVideoAd;

import java.util.Map;

public class BaiduATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private static final String TAG = BaiduATRewardedVideoAdapter.class.getSimpleName();

    RewardVideoAd mRewardVideoAd;
    private String mAdPlaceId = "";

    private void startLoadAd(Context context) {
        mRewardVideoAd = new RewardVideoAd(context.getApplicationContext(), mAdPlaceId, new RewardVideoAd.RewardVideoAdListener() {
            @Override
            public void onAdShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }

            @Override
            public void onAdClose(float v) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAdFailed(String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", s);
                }
            }

            @Override
            public void onVideoDownloadSuccess() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onVideoDownloadFailed() {

            }

            @Override
            public void playCompletion() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }
        });
        mRewardVideoAd.load();
    }

    @Override
    public boolean isAdReady() {
        if (mRewardVideoAd != null) {
            return mRewardVideoAd.isReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        try {
            mRewardVideoAd.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getNetworkName() {
        return BaiduATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String mAppId = (String) serverExtra.get("app_id");
        mAdPlaceId = (String) serverExtra.get("ad_place_id");
        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " app_id ,ad_place_id is empty.");
            }
            return;
        }

        BaiduATInitManager.getInstance().initSDK(context, serverExtra, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", e.getMessage());
                }
            }
        });
    }

    @Override
    public void destory() {
        mRewardVideoAd = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdPlaceId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return BaiduATInitManager.getInstance().getNetworkVersion();
    }

}
