package com.anythink.network.uniplay;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.uniplay.adsdk.InterstitialAd;
import com.uniplay.adsdk.InterstitialAdListener;

import java.util.Map;

public class UniplayATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = getClass().getSimpleName();

    String appId = "";
    private InterstitialAd mInterstitialAd;

    // Ad load listener
    InterstitialAdListener mAdListener = new InterstitialAdListener() {

        @Override
        public void onInterstitialAdReady() {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(UniplayATInterstitialAdapter.this);
            }
        }

        @Override
        public void onInterstitialAdShow() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdShow(UniplayATInterstitialAdapter.this);
            }
        }

        @Override
        public void onInterstitialAdClick() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked(UniplayATInterstitialAdapter.this);
            }
        }

        @Override
        public void onInterstitialAdFailed(String s) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(UniplayATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", s));
            }
        }

        @Override
        public void onInterstitialAdClose() {
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdClose(UniplayATInterstitialAdapter.this);
            }
        }
    };

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, final CustomInterstitialListener customRewardVideoListener) {

        mLoadResultListener = customRewardVideoListener;

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("app_id")) {
            appId = (String) serverExtras.get("app_id");

        } else {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id is empty!"));
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity"));
            }
            return;
        }

        mInterstitialAd = new InterstitialAd(context, appId);
        mInterstitialAd.setInterstitialAdListener(mAdListener);
        mInterstitialAd.loadInterstitialAd();
    }

    @Override
    public boolean isAdReady() {
        if (mInterstitialAd != null) {
            return mInterstitialAd.isInterstitialAdReady();
        }
        return false;
    }

    @Override
    public void show(Context context) {
        try {
            if (mInterstitialAd != null && context instanceof Activity) {
                mInterstitialAd.showInterstitialAd((Activity) context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clean() {
        mInterstitialAd = null;
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {

    }

    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return UniplayATInitManager.getInstance().getNetworkName();
    }
}
