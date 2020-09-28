package com.anythink.network.ks;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;

import java.util.Map;

public class KSATSplashAdapter extends CustomSplashAdapter {

    long posId;

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        if (!(context instanceof FragmentActivity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "kuaishou context must be FragmentActivity.");
            }
            return;
        } else if (View.NO_ID == mContainer.getId()) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "kuaishou must set ID for container.");
            }
            return;
        }

        String appId = (String) serverExtra.get("app_id");
        String position_id = (String) serverExtra.get("position_id");

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(position_id)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "kuaishou app_id or position_id is empty.");
            }
            return;
        }
        posId = Long.parseLong(position_id);

        KSATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtra);
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
            public void onSplashScreenAdLoad(@Nullable KsSplashScreenAd ksSplashScreenAd) {
                if (ksSplashScreenAd != null) {
                    Fragment fragment = ksSplashScreenAd.getFragment(new KsSplashScreenAd.SplashScreenAdInteractionListener() {
                        @Override
                        public void onAdClicked() {
                            if (mImpressionListener != null) {
                                mImpressionListener.onSplashAdClicked();
                            }
                        }

                        @Override
                        public void onAdShowError(int i, String s) {

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

                    if (fragment != null && mContainer != null) {
                        try {
                            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                                    .replace(mContainer.getId(), fragment)
                                    .commitAllowingStateLoss();

                            if (mLoadListener != null) {
                                mLoadListener.onAdCacheLoaded();
                            }
                        } catch (Throwable e) {
                            if (mLoadListener != null) {
                                mLoadListener.onAdLoadError("", e.getMessage());
                            }
                        }

                        return;
                    }
                }

                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "kuaishou splash no fill.");
                }
            }
        });

    }

    @Override
    public void destory() {

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
        return KSATConst.getSDKVersion();
    }

    @Override
    public String getNetworkName() {
        return KSATInitManager.getInstance().getNetworkName();
    }
}
