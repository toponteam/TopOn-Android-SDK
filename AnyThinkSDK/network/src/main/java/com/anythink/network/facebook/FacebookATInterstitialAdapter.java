package com.anythink.network.facebook;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */

public class FacebookATInterstitialAdapter extends CustomInterstitialAdapter {

    InterstitialAd mInterstitialAd;
    String mUnitid;

    String mPayload;

    /***
     * load ad
     */
    private void startLoad(final Context context) {

        final InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {

            @Override
            public void onError(Ad ad, AdError adError) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(FacebookATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, adError.getErrorCode() + "", "" + adError.getErrorMessage()));
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(FacebookATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked(FacebookATInterstitialAdapter.this);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }

            @Override
            public void onInterstitialDisplayed(Ad ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow(FacebookATInterstitialAdapter.this);
                }
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose(FacebookATInterstitialAdapter.this);
                }
            }
        };


        new Thread(new Runnable() {
            @Override
            public void run() {
                mInterstitialAd = new InterstitialAd(context, mUnitid);
                // Load a new interstitial.
                final InterstitialAd.InterstitialAdLoadConfigBuilder adConfig = mInterstitialAd.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener);

                if (!TextUtils.isEmpty(mPayload)) {
                    adConfig.withBid(mPayload);
                }

                mInterstitialAd.loadAd(adConfig.build());
            }
        }).start();
    }


    @Override
    public void clean() {
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }


    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "facebook serverExtras is empty."));
            }
            return;
        } else {


            if (serverExtras.containsKey("unit_id")) {
                mUnitid = (String) serverExtras.get("unit_id");
            } else {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "facebook sdkkey is empty."));
                }
                return;
            }
        }

        if (serverExtras.containsKey("payload")) {
            mPayload = serverExtras.get("payload").toString();
        }

        FacebookATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras);
        startLoad(context);
    }

    @Override
    public boolean isAdReady() {
        if (mInterstitialAd == null || !mInterstitialAd.isAdLoaded()) {
            return false;
        }

        if (mInterstitialAd.isAdInvalidated()) {
            return false;
        }

        return true;
    }

    @Override
    public void show(Context context) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show();
        }
    }

    @Override
    public String getSDKVersion() {
        return FacebookATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return FacebookATInitManager.getInstance().getNetworkName();
    }

}