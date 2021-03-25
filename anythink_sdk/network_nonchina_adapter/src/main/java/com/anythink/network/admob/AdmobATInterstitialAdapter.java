/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.admob;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Map;


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
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(errorCode + "", "");
                }
                AdMobATInitManager.getInstance().removeCache(getTrackingInfo().getmUnitGroupUnitId());
            }

            @Override
            public void onAdOpened() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onAdLeftApplication() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onAdClosed() {
                AdMobATInitManager.getInstance().removeCache(getTrackingInfo().getmUnitGroupUnitId());
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }
        });


        mAdRequest = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();


        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mInterstitialAd.loadAd(mAdRequest);
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }
        });

    }

    @Override
    public void destory() {
        try {
            if (mInterstitialAd != null) {
                mInterstitialAd.setAdListener(null);
                mAdRequest = null;
                mInterstitialAd = null;
                extras = null;
            }
        } catch (Exception e) {
        }
    }


    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {

        String appid = (String) serverExtras.get("app_id");
        unitid = (String) serverExtras.get("unit_id");

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitid)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "appid or unitId is empty.");
            }
            return;
        }

        //init
        AdMobATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new AdMobATInitManager.InitListener() {
            @Override
            public void initSuccess() {
                extras = AdMobATInitManager.getInstance().getRequestBundle(context.getApplicationContext());
                startLoadAd(context);
            }
        });
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

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AdMobATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    /***
     * Show Ad
     */
    @Override
    public void show(Activity activity) {
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
    public String getNetworkSDKVersion() {
        return AdMobATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return unitid;
    }
}