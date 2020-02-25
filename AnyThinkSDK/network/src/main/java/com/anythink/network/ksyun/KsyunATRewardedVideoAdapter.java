package com.anythink.network.ksyun;

import android.app.Activity;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.ksc.ad.sdk.IKsyunAdInitResultListener;
import com.ksc.ad.sdk.IKsyunAdListener;
import com.ksc.ad.sdk.IKsyunAdLoadListener;
import com.ksc.ad.sdk.IKsyunRewardVideoAdListener;
import com.ksc.ad.sdk.KsyunAdSdk;

import java.util.Map;

public class KsyunATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private final String TAG = getClass().getSimpleName();
    String mSlotid;

    /**
     * Ad load event callback
     */
    IKsyunAdLoadListener mAdLoadListener = new IKsyunAdLoadListener() {
        @Override
        public void onAdInfoSuccess() {
            log(TAG, "onAdInfoSuccess");
        }

        @Override
        public void onAdInfoFailed(int i, String s) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(KsyunATRewardedVideoAdapter.this,
                        ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(i), s));
            }
        }

        @Override
        public void onAdLoaded(String s) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdLoaded(KsyunATRewardedVideoAdapter.this);
            }

        }
    };

    /**
     * Advertising behavior event callback
     */
    IKsyunAdListener mAdListener = new IKsyunAdListener() {
        @Override
        public void onShowSuccess(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayStart(KsyunATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onShowFailed(String s, int i, String s1) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayFailed(KsyunATRewardedVideoAdapter.this,
                        ErrorCode.getErrorCode(ErrorCode.noADError, String.valueOf(i), s1));
            }
        }

        @Override
        public void onADComplete(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayEnd(KsyunATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onADClick(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayClicked(KsyunATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onADClose(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdClosed(KsyunATRewardedVideoAdapter.this);
            }
        }
    };

    /**
     * Advertising reward event callback
     */
    IKsyunRewardVideoAdListener mRewardListener = new IKsyunRewardVideoAdListener() {
        @Override
        public void onAdAwardSuccess(String s) {
            if (mImpressionListener != null) {
                mImpressionListener.onReward(KsyunATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onAdAwardFailed(String s, int i, String s1) {
        }
    };


    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {

        String mMediaId = "";
        if (serverExtras.containsKey("media_id")) {
            mMediaId = serverExtras.get("media_id").toString();
        }

        if (serverExtras.containsKey("slot_id")) {
            mSlotid = serverExtras.get("slot_id").toString();
        }


        mLoadResultListener = customRewardVideoListener;
        if (TextUtils.isEmpty(mMediaId) || TextUtils.isEmpty(mSlotid)) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "ksyun mediaid or slotid  is empty.");
                mLoadResultListener.onRewardedVideoAdFailed(this, adError);

            }
            return;
        }

        KsyunATInitManager.getInstance().initSDK(activity, serverExtras, new IKsyunAdInitResultListener() {
            @Override
            public void onSuccess(Map<String, String> map) {
                KsyunAdSdk.getInstance().setAdListener(mAdListener);
                KsyunAdSdk.getInstance().setRewardVideoAdListener(mRewardListener);
                KsyunAdSdk.getInstance().loadAd(mSlotid, mAdLoadListener);
            }

            @Override
            public void onFailure(int i, String s) {
                if(mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(KsyunATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", s));
                }
            }
        });
    }

    @Override
    public boolean isAdReady() {
        return KsyunAdSdk.getInstance().hasAd(mSlotid);
    }

    @Override
    public String getSDKVersion() {
        return KsyunATConst.getNetworkVersion();
    }

    @Override
    public void show(Activity activity) {
        KsyunAdSdk.getInstance().showAd(activity, mSlotid);
    }

    @Override
    public void clean() {
        KsyunAdSdk.getInstance().clearCache();
    }

    @Override
    public String getNetworkName() {
        return KsyunATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void onResume(Activity activity) {
        KsyunAdSdk.getInstance().onResume(activity);
    }

    @Override
    public void onPause(Activity activity) {
        KsyunAdSdk.getInstance().onPause(activity);
    }

}
