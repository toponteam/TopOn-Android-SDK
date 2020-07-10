package com.anythink.network.vungle;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.vungle.warren.AdConfig;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.Map;

public class VungleATInterstitialAdapter extends CustomInterstitialAdapter {

    private final String TAG = VungleATInterstitialAdapter.class.getSimpleName();
    String mPlacementId;
    AdConfig mAdConfig;

    private final LoadAdCallback loadAdCallback = new LoadAdCallback() {
        @Override
        public void onAdLoad(String s) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoaded(VungleATInterstitialAdapter.this);
            }
        }

        @Override
        public void onError(String s, VungleException throwable) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(VungleATInterstitialAdapter.this
                        , ErrorCode.getErrorCode(ErrorCode.noADError, "", throwable.toString()));
            }
        }
    };

    private final PlayAdCallback vungleDefaultListener = new PlayAdCallback() {

        @Override
        public void onAdEnd(String placementReferenceId, boolean wasSuccessFulView, boolean wasCallToActionClicked) {
            // Called when user exits the ad and control is returned to your application
            // if wasSuccessfulView is true, the user watched the ad and could be rewarded
            // if wasCallToActionClicked is true, the user clicked the call to action button in the ad.

            if (wasCallToActionClicked && mImpressListener != null) {
                mImpressListener.onInterstitialAdClicked(VungleATInterstitialAdapter.this);
            }

            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdVideoEnd(VungleATInterstitialAdapter.this);
                mImpressListener.onInterstitialAdClose(VungleATInterstitialAdapter.this);
            }
        }

        @Override
        public void onAdStart(String placementReferenceId) {
            // Called before playing an ad
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdShow(VungleATInterstitialAdapter.this);
                mImpressListener.onInterstitialAdVideoStart(VungleATInterstitialAdapter.this);
            }
        }

        @Override
        public void onError(String placementReferenceId, VungleException throwable) {
            // Called after playAd(placementId, adConfig) is unable to play the ad
            if (mImpressListener != null) {
                mImpressListener.onInterstitialAdVideoError(VungleATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", throwable.toString()));
            }
        }
    };


    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {

        String mAppId = (String) serverExtras.get("app_id");
        mPlacementId = (String) serverExtras.get("placement_id");

        mLoadResultListener = customInterstitialListener;

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mPlacementId)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "vungle appid & placementId is empty."));
            }
            return;
        }

        mAdConfig = new AdConfig();
        VungleATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new VungleATInitManager.InitListener() {
            @Override
            public void onSuccess() {
                Vungle.loadAd(mPlacementId, loadAdCallback);
            }

            @Override
            public void onError(Throwable throwable) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(VungleATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", throwable.getMessage()));
                }
            }
        });
    }

    @Override
    public boolean isAdReady() {
        return Vungle.canPlayAd(mPlacementId);
    }

    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public void show(Context context) {
        if (Vungle.canPlayAd(mPlacementId)) {
            // Play a Placement ad with Placement ID, you can pass AdConfig to customize your ad
            Vungle.playAd(mPlacementId, mAdConfig, vungleDefaultListener);
        }
    }

    @Override
    public void clean() {
    }

    @Override
    public String getNetworkName() {
        return VungleATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

}
