/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.network.kidoz;

import android.app.Activity;
import android.content.Context;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.kidoz.sdk.api.KidozInterstitial;
import com.kidoz.sdk.api.ui_views.interstitial.BaseInterstitial;

import java.util.Map;

public class KidozATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private static final String TAG = KidozATRewardedVideoAdapter.class.getSimpleName();

    private KidozInterstitial mKidozInterstitial;

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            mKidozInterstitial.show();
        }
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Kidoz context must be activity.");
            }
            return;
        }

        if (!serverExtra.containsKey("publisher_id")) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Kidoz publisher_id = null");
            }
            return;
        }

        if (!serverExtra.containsKey("security_token")) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Kidoz security_token = null");
            }
            return;
        }

        KidozATInitManager.getInstance().initSDK(context, serverExtra, new KidozATInitManager.InitListener() {
            @Override
            public void onSuccess() {
                startLoadAd(((Activity) context));
            }

            @Override
            public void onError(String errorMsg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "Kidoz " + errorMsg);
                }
            }
        });
    }

    private void startLoadAd(Activity activity) {
        mKidozInterstitial = new KidozInterstitial(activity, KidozInterstitial.AD_TYPE.REWARDED_VIDEO);
        mKidozInterstitial.setOnInterstitialEventListener(new BaseInterstitial.IOnInterstitialEventListener() {
            @Override
            public void onClosed() {
                //Informs when interstitial ad view has been close
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onOpened() {
                //Informs when interstitial ad view has been opened
            }

            @Override
            public void onReady() {
                //Lounch Interstitial when ready if needed
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onLoadFailed() {
                //Informs when interstitial ad view has failed to load
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "Kidoz load failed");
                }
            }

            @Override
            public void onNoOffers() {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "Kidoz no offers");
                }
            }
        });

        mKidozInterstitial.setOnInterstitialRewardedEventListener(new BaseInterstitial.IOnInterstitialRewardedEventListener() {
            @Override
            public void onRewardReceived() {
                //Informs when interstitial rewarded event is invoked (Rewarded video is completed)

                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            @Override
            public void onRewardedStarted() {
                //Informs when interstitial rewarded video started event
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }
        });

        mKidozInterstitial.loadAd();
    }

    @Override
    public void destory() {
        if (mKidozInterstitial != null) {
            mKidozInterstitial.setOnInterstitialEventListener(null);
            mKidozInterstitial.setOnInterstitialRewardedEventListener(null);
            mKidozInterstitial = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return "";
    }

    @Override
    public String getNetworkSDKVersion() {
        return KidozATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return KidozATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {
        return mKidozInterstitial != null && mKidozInterstitial.isLoaded();
    }
}
