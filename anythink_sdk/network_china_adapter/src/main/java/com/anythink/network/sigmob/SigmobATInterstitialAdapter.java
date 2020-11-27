/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.sigmob;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.fullscreenvideo.WindFullScreenAdRequest;
import com.sigmob.windad.fullscreenvideo.WindFullScreenVideoAd;
import com.sigmob.windad.fullscreenvideo.WindFullScreenVideoAdListener;
import com.sigmob.windad.rewardedVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAd;

import java.util.Map;

public class SigmobATInterstitialAdapter extends CustomInterstitialAdapter implements WindFullScreenVideoAdListener {

    private static final String TAG = SigmobATInterstitialAdapter.class.getSimpleName();
    private WindFullScreenAdRequest windFullScreenAdRequest;
    private String mPlacementId = "";

    private WindRewardAdRequest windVideoAdRequest;

    boolean isUseRewardedVideoAsInterstital = false;

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String appId = "";
        String appKey = "";
        if (serverExtra.containsKey("app_id")) {
            appId = serverExtra.get("app_id").toString();
        }
        if (serverExtra.containsKey("app_key")) {
            appKey = serverExtra.get("app_key").toString();
        }
        if (serverExtra.containsKey("placement_id")) {
            mPlacementId = serverExtra.get("placement_id").toString();
        }

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey) || TextUtils.isEmpty(mPlacementId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id、app_key、placement_id could not be null.");
            }
            return;
        }

        try {
            isUseRewardedVideoAsInterstital = ((boolean) localExtra.get(SigmobATConst.IS_USE_REWARDED_VIDEO_AS_INTERSTITIAL));
        } catch (Exception e) {
        }

        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    SigmobATInitManager.getInstance().initSDK(context, serverExtra, new SigmobATInitManager.InitCallback() {
                        @Override
                        public void onFinish() {
                            if (isUseRewardedVideoAsInterstital) {
                                windVideoAdRequest = new WindRewardAdRequest(mPlacementId, "", null);
                                SigmobATInitManager.getInstance().loadRewardedVideo(mPlacementId, windVideoAdRequest, SigmobATInterstitialAdapter.this);
                            } else {
                                windFullScreenAdRequest = new WindFullScreenAdRequest(mPlacementId, "", null);
                                SigmobATInitManager.getInstance().loadInterstitial(mPlacementId, windFullScreenAdRequest, SigmobATInterstitialAdapter.this);
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
    public void show(Activity activity) {
        try {
            //Check if the ad is ready
            if (this.isAdReady() && activity != null) {
                SigmobATInitManager.getInstance().putAdapter(mPlacementId, this);
                //show ad
                if (isUseRewardedVideoAsInterstital) {
                    WindRewardedVideoAd.sharedInstance().show(activity, windVideoAdRequest);
                } else {
                    WindFullScreenVideoAd.sharedInstance().show(activity, windFullScreenAdRequest);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isAdReady() {
        if (isUseRewardedVideoAsInterstital) {
            return WindRewardedVideoAd.sharedInstance() != null && WindRewardedVideoAd.sharedInstance().isReady(mPlacementId);
        } else {
            return WindFullScreenVideoAd.sharedInstance() != null && WindFullScreenVideoAd.sharedInstance().isReady(mPlacementId);
        }
    }

    @Override
    public String getNetworkName() {
        return SigmobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        windVideoAdRequest = null;
        windFullScreenAdRequest = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return SigmobATConst.getSDKVersion();
    }

    @Override
    public void onFullScreenVideoAdLoadSuccess(String s) {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }

        try {
            SigmobATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mPlacementId);
        } catch (Throwable e) {

        }
    }

    @Override
    public void onFullScreenVideoAdPreLoadSuccess(String s) {
    }

    @Override
    public void onFullScreenVideoAdPreLoadFail(String s) {
    }

    @Override
    public void onFullScreenVideoAdPlayStart(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow();
            mImpressListener.onInterstitialAdVideoStart();
        }
    }

    @Override
    public void onFullScreenVideoAdPlayEnd(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoEnd();
        }
    }

    @Override
    public void onFullScreenVideoAdClicked(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked();
        }
    }

    @Override
    public void onFullScreenVideoAdClosed(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose();
        }

        SigmobATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
    }

    @Override
    public void onFullScreenVideoAdLoadError(WindAdError windAdError, String s) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError("" + windAdError.getErrorCode(), windAdError.toString());
        }
    }

    @Override
    public void onFullScreenVideoAdPlayError(WindAdError windAdError, String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoError("" + windAdError.getErrorCode(), windAdError.toString());
        }
    }
}
