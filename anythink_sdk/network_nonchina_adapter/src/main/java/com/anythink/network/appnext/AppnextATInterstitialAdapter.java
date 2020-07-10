package com.anythink.network.appnext;

import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.appnext.ads.interstitial.Interstitial;
import com.appnext.core.AppnextAdCreativeType;
import com.appnext.core.callbacks.OnAdClicked;
import com.appnext.core.callbacks.OnAdClosed;
import com.appnext.core.callbacks.OnAdError;
import com.appnext.core.callbacks.OnAdLoaded;
import com.appnext.core.callbacks.OnAdOpened;

import java.util.Map;

public class AppnextATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = AppnextATInterstitialAdapter.class.getSimpleName();

    String mPlacementId;

    Interstitial mInterstitial;

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {

        mLoadResultListener = customInterstitialListener;

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(AppnextATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        }

        if (serverExtras.containsKey("placement_id")) {
            mPlacementId = (String) serverExtras.get("placement_id");

        } else {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(AppnextATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "placement_id is empty!"));
            }
            return;
        }

        AppnextATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras);

        mInterstitial = new Interstitial(context.getApplicationContext(), mPlacementId);

        // Get callback for ad loaded
        mInterstitial.setOnAdLoadedCallback(new OnAdLoaded() {
            @Override
            public void adLoaded(String s, AppnextAdCreativeType appnextAdCreativeType) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(AppnextATInterstitialAdapter.this);
                }
            }
        });// Get callback for ad opened
        mInterstitial.setOnAdOpenedCallback(new OnAdOpened() {
            @Override
            public void adOpened() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow(AppnextATInterstitialAdapter.this);
                }
            }
        });// Get callback for ad clicked
        mInterstitial.setOnAdClickedCallback(new OnAdClicked() {
            @Override
            public void adClicked() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked(AppnextATInterstitialAdapter.this);
                }
            }
        });// Get callback for ad closed
        mInterstitial.setOnAdClosedCallback(new OnAdClosed() {
            @Override
            public void onAdClosed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose(AppnextATInterstitialAdapter.this);
                }
            }
        });// Get callback for ad error
        mInterstitial.setOnAdErrorCallback(new OnAdError() {
            @Override
            public void adError(String error) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(AppnextATInterstitialAdapter.this
                            , ErrorCode.getErrorCode(ErrorCode.noADError, "", error));
                }
            }
        });

        mInterstitial.loadAd();

    }

    @Override
    public boolean isAdReady() {
        if (mInterstitial != null) {
            return mInterstitial.isAdLoaded();
        }
        return false;
    }

    @Override
    public void show(Context context) {
        if (mInterstitial != null) {
            mInterstitial.showAd();
        }
    }

    @Override
    public void clean() {
        if (mInterstitial != null) {
            mInterstitial.destroy();
        }
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
        return AppnextATInitManager.getInstance().getNetworkName();
    }
}
