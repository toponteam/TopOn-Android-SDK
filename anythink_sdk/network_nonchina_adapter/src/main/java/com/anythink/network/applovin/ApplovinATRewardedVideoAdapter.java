package com.anythink.network.applovin;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.applovin.adview.AppLovinIncentivizedInterstitial;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdRewardListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinSdk;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */

public class ApplovinATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private static final String TAG = ApplovinATRewardedVideoAdapter.class.getSimpleName();

    AppLovinIncentivizedInterstitial mInterstitial;

    AppLovinAdRewardListener mAppLovinAdRewardListener;
    AppLovinAdVideoPlaybackListener mAppLovinAdVideoPlaybackListener;
    AppLovinAdDisplayListener mAppLovinAdDisplayListener;
    AppLovinAdClickListener mAppLovinAdClickListener;

    String sdkkey = "", zoneid = "";
    ApplovinRewardedVideoSetting mApplovinMediationSetting;

    boolean isReward = false;

    /***
     * init
     */
    private void init(Context context, Map<String, Object> serverExtras) {

        AppLovinSdk mApplovinSdk = ApplovinATInitManager.getInstance().initSDK(context, sdkkey, serverExtras);

        mInterstitial = AppLovinIncentivizedInterstitial.create(zoneid, mApplovinSdk);

        mAppLovinAdRewardListener = new AppLovinAdRewardListener() {
            @Override
            public void userRewardVerified(AppLovinAd pAppLovinAd, Map<String, String> pMap) {
            }

            @Override
            public void userOverQuota(AppLovinAd pAppLovinAd, Map<String, String> pMap) {
            }

            @Override
            public void userRewardRejected(AppLovinAd pAppLovinAd, Map<String, String> pMap) {
            }

            @Override
            public void validationRequestFailed(AppLovinAd pAppLovinAd, int pI) {
            }

            @Override
            public void userDeclinedToViewAd(AppLovinAd pAppLovinAd) {
            }
        };


        mAppLovinAdVideoPlaybackListener = new AppLovinAdVideoPlaybackListener() {
            @Override
            public void videoPlaybackBegan(AppLovinAd appLovinAd) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(ApplovinATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(ApplovinATRewardedVideoAdapter.this);
                }
                if (mImpressionListener != null && b) {
                    mImpressionListener.onReward(ApplovinATRewardedVideoAdapter.this);
                }
            }
        };
        mAppLovinAdDisplayListener = new AppLovinAdDisplayListener() {
            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {

            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed(ApplovinATRewardedVideoAdapter.this);
                }
                isReward = false;
            }
        };
        mAppLovinAdClickListener = new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(ApplovinATRewardedVideoAdapter.this);
                }
            }
        };
        mInterstitial.setUserIdentifier(mUserId);

    }

    /***
     * load ad
     */
    public void startload() {

        if (check()) {
            mInterstitial.preload(new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd appLovinAd) {
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onRewardedVideoAdLoaded(ApplovinATRewardedVideoAdapter.this);
                    }
                }

                @Override
                public void failedToReceiveAd(int errorCode) {
                    if (mLoadResultListener != null) {
                        mLoadResultListener.onRewardedVideoAdFailed(ApplovinATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " " + errorCode));
                    }
                }
            });
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
        if (mediationSetting != null && mediationSetting instanceof ApplovinRewardedVideoSetting) {
            mApplovinMediationSetting = (ApplovinRewardedVideoSetting) mediationSetting;

        }


        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service info  is empty."));
            }
            return;
        } else {
            if (serverExtras.containsKey("sdkkey") && serverExtras.containsKey("zone_id")) {
                sdkkey = (String) serverExtras.get("sdkkey");
                zoneid = (String) serverExtras.get("zone_id");
            } else {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "sdkkey or zone_id is empty!"));
                }
                return;
            }
        }

        init(activity.getApplicationContext(), serverExtras);
        startload();
    }

    @Override
    public boolean isAdReady() {
        if (check()) {
            return mInterstitial.isAdReadyToDisplay();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (check() && mInterstitial.isAdReadyToDisplay()) {
            mInterstitial.show(activity, mAppLovinAdRewardListener, mAppLovinAdVideoPlaybackListener, mAppLovinAdDisplayListener, mAppLovinAdClickListener);
        }
    }

    private boolean check() {
        return mInterstitial != null;
    }


    @Override
    public String getSDKVersion() {
        return ApplovinATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return ApplovinATInitManager.getInstance().getNetworkName();
    }

}