/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;

import java.util.Map;


public class AdmobATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private static final String TAG = AdmobATRewardedVideoAdapter.class.getSimpleName();

    RewardedAd mRewardedAd;
    AdRequest mAdRequest = null;
    private String unitid = "";


    Bundle extras = new Bundle();

    boolean isPlayComplete = false;

    RewardedVideoAd mRewardedVideoAd;

    boolean isAdReady = false;

    /***
     * init
     */
    private void init(Context context) {

        boolean existRewardAD = false;
        try {
            Class.forName("com.google.android.gms.ads.rewarded.RewardedAd");
            existRewardAD = true;
        } catch (Exception e) {

        }

        if (existRewardAD) {
            mRewardedAd = new RewardedAd(context.getApplicationContext(), unitid);
        } else {
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context.getApplicationContext());
            mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    isAdReady = true;
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onRewardedVideoAdOpened() {

                }

                @Override
                public void onRewardedVideoStarted() {
                    isPlayComplete = false;
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayStart();
                    }
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdClosed();
                    }
                }

                @Override
                public void onRewarded(RewardItem pRewardItem) {

                    if (!isPlayComplete) {
                        isPlayComplete = true;
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayEnd();
                        }
                        if (mImpressionListener != null) {
                            mImpressionListener.onReward();
                        }
                    }

                }

                @Override
                public void onRewardedVideoAdLeftApplication() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayClicked();
                    }
                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int pErrorCode) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(String.valueOf(pErrorCode), "");
                    }
                }

                public void onRewardedVideoCompleted() {
                    if (!isPlayComplete) {
                        isPlayComplete = true;
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayEnd();
                        }
                        if (mImpressionListener != null) {
                            mImpressionListener.onReward();
                        }
                    }
                }
            });
        }


        mAdRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();

        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mRewardedAd != null) {
                        mRewardedAd.setServerSideVerificationOptions(new ServerSideVerificationOptions.Builder()
                                .setUserId(mUserId)
                                .setCustomData(mUserData)
                                .build()
                        );
                        mRewardedAd.loadAd(mAdRequest, new RewardedAdLoadCallback() {
                            @Override
                            public void onRewardedAdLoaded() {
                                isAdReady = true;
                                AdMobATInitManager.getInstance().addCache(getTrackingInfo().getmUnitGroupUnitId(), mRewardedAd);
                                if (mLoadListener != null) {
                                    mLoadListener.onAdCacheLoaded();
                                }
                            }

                            @Override
                            public void onRewardedAdFailedToLoad(int i) {
                                if (mLoadListener != null) {
                                    mLoadListener.onAdLoadError(String.valueOf(i), "");
                                }
                                AdMobATInitManager.getInstance().removeCache(getTrackingInfo().getmUnitGroupUnitId());
                            }
                        });
                    } else {
                        mRewardedVideoAd.setUserId(mUserId);
                        mRewardedVideoAd.setCustomData(mUserData);
                        mRewardedVideoAd.loadAd(unitid, mAdRequest);
                    }
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }

        });

    }


    @Override
    public void destory() {
        try {
            mRewardedAd = null;
            if (mRewardedVideoAd != null) {
                mRewardedVideoAd.destroy(null);
                mRewardedVideoAd = null;
            }
            mAdRequest = null;
            extras = null;
        } catch (Exception e) {
        }
    }


    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {


        String appid = (String) serverExtras.get("app_id");
        unitid = (String) serverExtras.get("unit_id");

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitid)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "appid or unitId is empty.");
            }
            return;
        }

        AdMobATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new AdMobATInitManager.InitListener() {
            @Override
            public void initSuccess() {
                extras = AdMobATInitManager.getInstance().getRequestBundle(context.getApplicationContext());
                init(context);
            }
        });
    }

    @Override
    public boolean isAdReady() {
        try {
            if (mRewardedAd != null) {
                return mRewardedAd.isLoaded();
            }

            if (mRewardedVideoAd != null) {
                return mRewardedVideoAd.isLoaded();
            }
        } catch (Throwable e) {

        }
        return isAdReady;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AdMobATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        if (mRewardedAd != null) {
            if (activity != null) {
                mRewardedAd.show(activity, new RewardedAdCallback() {
                    @Override
                    public void onRewardedAdClosed() {
                        AdMobATInitManager.getInstance().removeCache(getTrackingInfo().getmUnitGroupUnitId());
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdClosed();
                        }
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayFailed(String.valueOf(i), "");
                        }
                    }

                    @Override
                    public void onRewardedAdOpened() {
                        isPlayComplete = false;
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayStart();
                        }
                    }

                    @Override
                    public void onUserEarnedReward(com.google.android.gms.ads.rewarded.RewardItem rewardItem) {

                        if (!isPlayComplete) {
                            isPlayComplete = true;
                            if (mImpressionListener != null) {
                                mImpressionListener.onRewardedVideoAdPlayEnd();
                            }
                        }

                        if (mImpressionListener != null) {
                            mImpressionListener.onReward();
                        }
                    }
                });

            }
        }

        if (mRewardedVideoAd != null) {
            mRewardedVideoAd.show();
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return AdMobATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return unitid;
    }

}