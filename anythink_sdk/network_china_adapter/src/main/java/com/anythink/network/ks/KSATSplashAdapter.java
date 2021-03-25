/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ks;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;

import java.util.Map;

public class KSATSplashAdapter extends CustomSplashAdapter {
    private final String TAG = getClass().getSimpleName();
    long posId;
    KsSplashScreenAd mKsSplashScreenAd;

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String appId = (String) serverExtra.get("app_id");
        String position_id = (String) serverExtra.get("position_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(position_id)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "kuaishou app_id or position_id is empty.");
            }
            return;
        }
        posId = Long.parseLong(position_id);

        KSATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtra, new KSATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                startLoadAd();
            }
        });
    }

    private void startLoadAd() {
        KsScene adScene = new KsScene.Builder(posId)
                .adNum(1)
                .build();

        KsAdSDK.getLoadManager().loadSplashScreenAd(adScene, new KsLoadManager.SplashScreenAdListener() {
            @Override
            public void onError(int i, String s) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(String.valueOf(i), s);
                }
            }

            @Override
            public void onRequestResult(int i) {

            }

            @Override
            public void onSplashScreenAdLoad(@Nullable KsSplashScreenAd ksSplashScreenAd) {
                mKsSplashScreenAd = ksSplashScreenAd;

                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }
        });
    }

    @Override
    public boolean isAdReady() {
        return mKsSplashScreenAd != null;
    }

    @Override
    public void show(Activity activity, ViewGroup container) {

        if (mKsSplashScreenAd != null) {
            View splashView = mKsSplashScreenAd.getView(activity, new KsSplashScreenAd.SplashScreenAdInteractionListener() {

                @Override
                public void onAdClicked() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onSplashAdClicked();
                    }
                }

                @Override
                public void onAdShowError(int code, String extra) {
                    Log.e(TAG, "onAdShowError: " + code + ", " + extra);
                }

                @Override
                public void onAdShowEnd() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onSplashAdDismiss();
                    }
                }

                @Override
                public void onAdShowStart() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onSplashAdShow();
                    }
                }

                @Override
                public void onSkippedAd() {
                    if (mImpressionListener != null) {
                        mImpressionListener.onSplashAdDismiss();
                    }
                }
            });

            try {
                container.addView(splashView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destory() {
        mKsSplashScreenAd = null;
    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(posId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    @Override
    public String getNetworkSDKVersion() {
        return KSATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return KSATInitManager.getInstance().getNetworkName();
    }
}
