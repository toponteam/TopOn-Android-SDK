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
import com.sigmob.windad.interstitial.WindInterstitialAd;
import com.sigmob.windad.interstitial.WindInterstitialAdListener;
import com.sigmob.windad.interstitial.WindInterstitialAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAd;

import java.util.Map;

public class SigmobATInterstitialAdapter extends CustomInterstitialAdapter implements WindInterstitialAdListener {

    private static final String TAG = SigmobATInterstitialAdapter.class.getSimpleName();
    private WindInterstitialAdRequest windInterstitialAdRequest;
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
                                windInterstitialAdRequest = new WindInterstitialAdRequest(mPlacementId, "", null);
                                SigmobATInitManager.getInstance().loadInterstitial(mPlacementId, windInterstitialAdRequest, SigmobATInterstitialAdapter.this);
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
                    WindInterstitialAd.sharedInstance().show(activity, windInterstitialAdRequest);
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
            return WindInterstitialAd.sharedInstance() != null && WindInterstitialAd.sharedInstance().isReady(mPlacementId);
        }
    }

    @Override
    public String getNetworkName() {
        return SigmobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        windVideoAdRequest = null;
        windInterstitialAdRequest = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return SigmobATInitManager.getInstance().getNetworkVersion();
    }

    public void onInterstitialAdLoadSuccess(String s) {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }

        try {
            SigmobATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mPlacementId);
        } catch (Throwable e) {

        }
    }

    public void onInterstitialAdPreLoadSuccess(String s) {
    }

    public void onInterstitialAdPreLoadFail(String s) {
    }

    public void onInterstitialAdPlayStart(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow();
            mImpressListener.onInterstitialAdVideoStart();
        }
    }

    public void onInterstitialAdPlayEnd(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoEnd();
        }
    }

    public void onInterstitialAdClicked(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked();
        }
    }

    public void onInterstitialAdClosed(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose();
        }

        SigmobATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
    }

    public void onInterstitialAdLoadError(WindAdError windAdError, String s) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError("" + windAdError.getErrorCode(), windAdError.toString());
        }
    }

    @Override
    public void onInterstitialAdPlayError(WindAdError windAdError, String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoError("" + windAdError.getErrorCode(), windAdError.toString());
        }
    }
}
