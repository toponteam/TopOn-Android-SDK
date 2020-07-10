package com.anythink.network.admob;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Map;

/**
 * RewardVideo adapter admob
 * https://developers.google.com/admob/android/rewarded-video
 * Created by zhou on 2018/6/27.
 */

public class AdmobATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private static final String TAG = AdmobATRewardedVideoAdapter.class.getSimpleName();

    RewardedAd mRewardedAd;
    AdRequest mAdRequest = null;
    private String unitid = "";


    AdmobRewardedVideoSetting mAdmobATMediationSetting;

    Bundle extras = new Bundle();

    boolean isPlayComplete = false;

    RewardedVideoAd mRewardedVideoAd;

    boolean isAdReady = false;

    /***
     * init
     */
    private void init(Activity activity) {

        boolean exitRewardAD = false;
        try {
            Class.forName("com.google.android.gms.ads.rewarded.RewardedAd");
            exitRewardAD = true;
        } catch (Exception e) {

        }

        if (exitRewardAD) {
            mRewardedAd = new RewardedAd(activity.getApplicationContext(), unitid);
        } else {
            mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity.getApplicationContext());
            mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    isAdReady = true;
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onRewardedVideoAdLoaded(AdmobATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onRewardedVideoAdOpened() {

                }

                @Override
                public void onRewardedVideoStarted() {
                    isPlayComplete = false;
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayStart(AdmobATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdClosed(AdmobATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onRewarded(RewardItem pRewardItem) {

                    if (!isPlayComplete) {
                        isPlayComplete = true;
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayEnd(AdmobATRewardedVideoAdapter.this);
                        }
                        if (mImpressionListener != null) {
                            mImpressionListener.onReward(AdmobATRewardedVideoAdapter.this);
                        }
                    }

                }

                @Override
                public void onRewardedVideoAdLeftApplication() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayClicked(AdmobATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int pErrorCode) {
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onRewardedVideoAdFailed(AdmobATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "" + pErrorCode));
                    }
                }

                public void onRewardedVideoCompleted() {
                    if (!isPlayComplete) {
                        isPlayComplete = true;
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayEnd(AdmobATRewardedVideoAdapter.this);
                        }
                        if (mImpressionListener != null) {
                            mImpressionListener.onReward(AdmobATRewardedVideoAdapter.this);
                        }
                    }
                }
            });
        }


        mAdRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();

        if (mRewardedAd != null) {
            mRewardedAd.loadAd(mAdRequest, new RewardedAdLoadCallback() {
                @Override
                public void onRewardedAdLoaded() {
                    isAdReady = true;
                    AdMobATInitManager.getInstance().addCache(getTrackingInfo().getmUnitGroupUnitId(), mRewardedAd);
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onRewardedVideoAdLoaded(AdmobATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onRewardedAdFailedToLoad(int i) {
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onRewardedVideoAdFailed(AdmobATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "" + i));
                    }
                    AdMobATInitManager.getInstance().removeCache(getTrackingInfo().getmUnitGroupUnitId());
                }
            });
        } else {
            mRewardedVideoAd.loadAd(unitid, mAdRequest);
        }
    }


    @Override
    public void clean() {
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {
    }


    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;

        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof AdmobRewardedVideoSetting) {
            mAdmobATMediationSetting = (AdmobRewardedVideoSetting) mediationSetting;
        }


        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid or unitid  is empty."));
            }
            return;
        } else {
            String appid = (String) serverExtras.get("app_id");
            unitid = (String) serverExtras.get("unit_id");

            if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitid)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid ,unitid or sdkkey is empty."));

                }
                return;
            }
        }

        AdMobATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras);
        extras = AdMobATInitManager.getInstance().getRequestBundle(activity.getApplicationContext());

        init(activity);

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
    public void show(Activity activity) {
        if (mRewardedAd != null) {
            mRewardedAd.show(activity, new RewardedAdCallback() {
                @Override
                public void onRewardedAdClosed() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdClosed(AdmobATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onRewardedAdFailedToShow(int i) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayFailed(AdmobATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "" + i));
                    }
                }

                @Override
                public void onRewardedAdOpened() {
                    isPlayComplete = false;
                    if (mImpressionListener != null) {
                        mImpressionListener.onRewardedVideoAdPlayStart(AdmobATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void onUserEarnedReward(com.google.android.gms.ads.rewarded.RewardItem rewardItem) {

                    if (!isPlayComplete) {
                        isPlayComplete = true;
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayEnd(AdmobATRewardedVideoAdapter.this);
                        }
                    }

                    if (mImpressionListener != null) {
                        mImpressionListener.onReward(AdmobATRewardedVideoAdapter.this);
                    }
                }
            });

        }

        if (mRewardedVideoAd != null) {
            mRewardedVideoAd.show();
        }
    }

    @Override
    public String getSDKVersion() {
        return AdmobATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getNetworkName();
    }

}