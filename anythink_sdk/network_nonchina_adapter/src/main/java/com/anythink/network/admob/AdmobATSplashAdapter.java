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
import android.util.Log;
import android.view.ViewGroup;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Map;

public class AdmobATSplashAdapter extends CustomSplashAdapter {

    public static final String TAG = AdmobATSplashAdapter.class.getSimpleName();

    private String unitid = "";
    private int orientation;
    Bundle extras = new Bundle();

    boolean isDestroyed = false;

    AppOpenAd.AppOpenAdLoadCallback loadCallback;
    FullScreenContentCallback fullScreenContentCallback;

    AppOpenAd mAppOpenAd;

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String appid = (String) serverExtra.get("app_id");
        unitid = (String) serverExtra.get("unit_id");

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitid)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "appid or unitId is empty.");
            }
            return;
        }

        orientation = -1;
        try {
            Object orientationObj = serverExtra.get("orientation");
            if (orientationObj != null) {
                orientation = Integer.parseInt((String) orientationObj);
            }
        } catch (Throwable e) {
        }

        if (orientation != AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT && orientation != AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE) {
            Log.e(TAG, "Admob splash orientation error: " + orientation);
            orientation = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT;
        }


        AdMobATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtra, new AdMobATInitManager.InitListener() {
            @Override
            public void initSuccess() {
                extras = AdMobATInitManager.getInstance().getRequestBundle(context.getApplicationContext());
                startLoadAd(context);
            }
        });
    }

    private void startLoadAd(final Context context) {

        loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */
            @Override
            public void onAppOpenAdLoaded(final AppOpenAd ad) {
                AdMobATInitManager.getInstance().removeCache(AdmobATSplashAdapter.this.toString());
                mAppOpenAd = ad;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            @Override
            public void onAppOpenAdFailedToLoad(LoadAdError loadAdError) {
                // Handle the error.
                AdMobATInitManager.getInstance().removeCache(AdmobATSplashAdapter.this.toString());
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(loadAdError.getCode() + "", loadAdError.getMessage());
                }
            }

        };

        //Keep the callback object alive.
        //If not call this, it would not have any response int the callback listener
        AdMobATInitManager.getInstance().addCache(toString(), this);
        final AdRequest request = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();

        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                AppOpenAd.load(context, unitid, request, orientation, loadCallback);
            }
        });

    }

    @Override
    public boolean isAdReady() {
        return mAppOpenAd != null;
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        fullScreenContentCallback = new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                Log.e(TAG, "Admob splash show fail: " + adError.getCode() + ", " + adError.getMessage());
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdShow();
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }
        };

        mAppOpenAd.show(activity, fullScreenContentCallback);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AdMobATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void destory() {
        loadCallback = null;
        fullScreenContentCallback = null;
        extras = null;
        isDestroyed = true;
    }

    @Override
    public String getNetworkPlacementId() {
        return unitid;
    }

    @Override
    public String getNetworkSDKVersion() {
        return AdMobATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getNetworkName();
    }
}
