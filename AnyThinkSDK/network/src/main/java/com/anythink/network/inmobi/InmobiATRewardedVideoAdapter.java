package com.anythink.network.inmobi;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiInterstitial;
import com.inmobi.ads.listeners.InterstitialAdEventListener;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */

public class InmobiATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private static final String TAG = InmobiATRewardedVideoAdapter.class.getSimpleName();

    InMobiInterstitial interstitialAd;
    InmobiRewardedVideoSetting mInmobiMediationSetting;
    Long placeId;

    int mClickCallbackType;


    /***
     * init and load
     */
    private void initAndLoad(final Context context, final Map<String, Object> serverExtras) {
        InmobiATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new InmobiATInitManager.OnInitCallback() {
            @Override
            public void onFinish() {
                interstitialAd = new InMobiInterstitial(context, placeId, new InterstitialAdEventListener() {
                    @Override
                    public void onAdLoadFailed(InMobiInterstitial inMobiInterstitial, InMobiAdRequestStatus inMobiAdRequestStatus) {
                        if (mLoadResultListener != null) {
                            mLoadResultListener.onRewardedVideoAdFailed(InmobiATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + inMobiAdRequestStatus.getStatusCode(), inMobiAdRequestStatus.getMessage()));
                        }
                    }

                    @Override
                    public void onAdReceived(InMobiInterstitial inMobiInterstitial) {
                        if (mLoadResultListener != null) {
                            mLoadResultListener.onRewardedVideoAdDataLoaded(InmobiATRewardedVideoAdapter.this);
                        }
                    }

                    @Override
                    public void onAdLoadSucceeded(InMobiInterstitial inMobiInterstitial) {
                        if (mLoadResultListener != null) {
                            mLoadResultListener.onRewardedVideoAdLoaded(InmobiATRewardedVideoAdapter.this);
                        }
                    }

                    @Override
                    public void onRewardsUnlocked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayEnd(InmobiATRewardedVideoAdapter.this);
                        }

                        if (mImpressionListener != null) {
                            mImpressionListener.onReward(InmobiATRewardedVideoAdapter.this);
                        }
                    }

                    @Override
                    public void onAdDisplayFailed(InMobiInterstitial inMobiInterstitial) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayFailed(InmobiATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", "AdDisplayFailed"));
                        }
                    }

                    @Override
                    public void onAdWillDisplay(InMobiInterstitial inMobiInterstitial) {
                    }

                    @Override
                    public void onAdDisplayed(InMobiInterstitial inMobiInterstitial) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayStart(InmobiATRewardedVideoAdapter.this);
                        }
                    }

                    @Override
                    public void onAdClicked(InMobiInterstitial inMobiInterstitial, Map<Object, Object> map) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdPlayClicked(InmobiATRewardedVideoAdapter.this);
                        }
                    }


                    @Override
                    public void onAdDismissed(InMobiInterstitial inMobiInterstitial) {
                        if (mImpressionListener != null) {
                            mImpressionListener.onRewardedVideoAdClosed(InmobiATRewardedVideoAdapter.this);
                        }
                    }
                });
                interstitialAd.load();
            }
        });
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
        if (mediationSetting != null && mediationSetting instanceof InmobiRewardedVideoSetting) {
            mInmobiMediationSetting = (InmobiRewardedVideoSetting) mediationSetting;
        }
        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "inmobi serverExtras is null!"));
            }
            return;
        } else {

            String accountId = (String) serverExtras.get("app_id");
            String unitId = (String) serverExtras.get("unit_id");

            if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(unitId)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "inmobi account_id or unit_id is empty!"));
                }
                return;
            }
            placeId = Long.parseLong(unitId);
        }
        mClickCallbackType = 0;
        initAndLoad(activity, serverExtras);
    }

    @Override
    public boolean isAdReady() {
        if (interstitialAd != null) {
            return interstitialAd.isReady();
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (interstitialAd != null && isAdReady()) {
            interstitialAd.show();
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
    public String getSDKVersion() {
        return InmobiATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return InmobiATInitManager.getInstance().getNetworkName();
    }
}