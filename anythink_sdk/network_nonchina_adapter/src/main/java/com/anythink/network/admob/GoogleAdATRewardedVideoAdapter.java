package com.anythink.network.admob;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;

import java.util.Map;

/**
 * RewardedVideo Adapter for Google Ad Manager
 */

public class GoogleAdATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    RewardedAd mRewardedAd;
    PublisherAdRequest mAdRequest = null;
    private String unitid = "";


    boolean isPlayComplete = false;

    RewardedVideoAd mRewardedVideoAd;

    boolean isAdReady = false;

    /***
     * init
     */
    private void init(Context context) {

        boolean exitRewardAD = false;
        try {
            Class.forName("com.google.android.gms.ads.rewarded.RewardedAd");
            exitRewardAD = true;
        } catch (Exception e) {

        }

        if (exitRewardAD) {
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


        mAdRequest = new PublisherAdRequest.Builder().build();

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
                                if (mLoadListener != null) {
                                    mLoadListener.onAdCacheLoaded();
                                }
                            }

                            @Override
                            public void onRewardedAdFailedToLoad(int i) {
                                if (mLoadListener != null) {
                                    mLoadListener.onAdLoadError(String.valueOf(i), "");
                                }
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
        } catch (Exception e) {
        }
    }


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {


        unitid = (String) serverExtras.get("unit_id");

        if (TextUtils.isEmpty(unitid)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "unitid is empty.");
            }
            return;
        }

        init(context);

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
        return AdmobATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getGoogleAdManagerName();
    }

    @Override
    public String getNetworkPlacementId() {
        return unitid;
    }

}