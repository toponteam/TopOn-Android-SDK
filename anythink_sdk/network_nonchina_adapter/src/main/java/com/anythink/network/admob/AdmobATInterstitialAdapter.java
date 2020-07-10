package com.anythink.network.admob;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Map;

/**
 * Interstitial adapter admob
 * Created by Z on 2018/6/27.
 */

public class AdmobATInterstitialAdapter extends CustomInterstitialAdapter {
    private static final String TAG = AdmobATInterstitialAdapter.class.getSimpleName();

    InterstitialAd mInterstitialAd;
    AdRequest mAdRequest = null;
    private String unitid = "", appid = "";


    Bundle extras = new Bundle();

    boolean isAdReady = false;
    /***
     * load ad
     */
    private void startLoadAd(Context context) {

        mInterstitialAd = new InterstitialAd(context.getApplicationContext());
        mInterstitialAd.setAdUnitId(unitid);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                isAdReady = true;
                AdMobATInitManager.getInstance().addCache(getTrackingInfo().getmUnitGroupUnitId(), mInterstitialAd);
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(AdmobATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(AdmobATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, errorCode + "", ""));
                }
                AdMobATInitManager.getInstance().removeCache(getTrackingInfo().getmUnitGroupUnitId());
            }

            @Override
            public void onAdOpened() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow(AdmobATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdLeftApplication() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked(AdmobATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose(AdmobATInterstitialAdapter.this);
                }
            }
        });


        mAdRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();

        mInterstitialAd.loadAd(mAdRequest);

    }

    @Override
    public void clean() {
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
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid or unitid  is empty."));
            }
            return;
        } else {

            String appid = (String) serverExtras.get("app_id");
            unitid = (String) serverExtras.get("unit_id");

            if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitid)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid ,unitid or sdkkey is empty."));

                }
                return;
            }
        }

        //初始化
        AdMobATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras);

        extras = AdMobATInitManager.getInstance().getRequestBundle(context.getApplicationContext());

        startLoadAd(context);

    }

    @Override
    public boolean isAdReady() {
        try {
            if (check()) {
                return mInterstitialAd.isLoaded();
            }
        } catch (Throwable e) {

        }
        return isAdReady;
    }

    /***
     * Show Ad
     */
    @Override
    public void show(Context context) {
        if (check()) {
            isAdReady = false;
            mInterstitialAd.show();
        }
    }

    private boolean check() {
        if (mInterstitialAd == null) {
            return false;
        }
        return true;
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