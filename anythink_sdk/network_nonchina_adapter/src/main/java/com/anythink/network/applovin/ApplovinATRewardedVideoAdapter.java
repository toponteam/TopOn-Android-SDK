package com.anythink.network.applovin;

import android.app.Activity;
import android.content.Context;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
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

    boolean isReward = false;

    /***
     * init
     */
    private void init(Context context, Map<String, Object> serverExtras) {

        AppLovinSdk mApplovinSdk = ApplovinATInitManager.getInstance().initSDK(context, sdkkey, serverExtras);
        mApplovinSdk.setUserIdentifier(mUserId);

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
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }
                if (mImpressionListener != null && b) {
                    mImpressionListener.onReward();
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
                    mImpressionListener.onRewardedVideoAdClosed();
                }
                isReward = false;
            }
        };
        mAppLovinAdClickListener = new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }
        };
    }

    /***
     * load ad
     */
    public void startload() {

        if (check()) {
            mInterstitial.preload(new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd appLovinAd) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void failedToReceiveAd(int errorCode) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(String.valueOf(errorCode), "");
                    }
                }
            });
        }
    }

    @Override
    public void destory() {
        mInterstitial = null;
        mAppLovinAdClickListener = null;
        mAppLovinAdDisplayListener = null;
        mAppLovinAdRewardListener = null;
        mAppLovinAdVideoPlaybackListener = null;
    }


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("sdkkey") && serverExtras.containsKey("zone_id")) {
            sdkkey = (String) serverExtras.get("sdkkey");
            zoneid = (String) serverExtras.get("zone_id");
        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "sdkkey or zone_id is empty!");
            }
            return;
        }

        init(context.getApplicationContext(), serverExtras);
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
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return ApplovinATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
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
    public String getNetworkSDKVersion() {
        return ApplovinATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return ApplovinATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return zoneid;
    }

}