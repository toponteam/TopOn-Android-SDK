/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.sigmob;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.sigmob.windad.Splash.WindSplashAD;
import com.sigmob.windad.Splash.WindSplashADListener;
import com.sigmob.windad.Splash.WindSplashAdRequest;
import com.sigmob.windad.WindAdError;

import java.util.Map;

public class SigmobATSplashAdapter extends CustomSplashAdapter {

    private static final String TAG = SigmobATSplashAdapter.class.getSimpleName();
    private String mPlacementId = "";
    private WindSplashAD mWindSplashAD;

    @Override
    public String getNetworkName() {
        return SigmobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {
        return mWindSplashAD != null && mWindSplashAD.isReady();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Sigmob: context must be activity");
            }
            return;
        }

        String appId = "";
        String appKey = "";
        if (serverExtra.containsKey("app_id")) {
            appId = serverExtra.get("app_id").toString();
        }
        if (serverExtra.containsKey("app_key")) {
            appKey = serverExtra.get("app_key").toString();
        }
        if (serverExtra.containsKey("placement_id")) {
            mPlacementId = serverExtra.get("placement_id").toString();
        }

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey) || TextUtils.isEmpty(mPlacementId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id、app_key、placement_id could not be null.");
            }
            return;
        }

        SigmobATInitManager.getInstance().initSDK(context, serverExtra, new SigmobATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                startLoadAd((Activity) context);
            }
        });
    }

    private void startLoadAd(final Activity context) {
        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                WindSplashAdRequest splashAdRequest = new WindSplashAdRequest(mPlacementId, "", null);
                splashAdRequest.setFetchDelay(mFetchAdTimeout / 1000);
                splashAdRequest.setDisableAutoHideAd(true);

                //show ad
                mWindSplashAD = new WindSplashAD(context, splashAdRequest, new WindSplashADListener() {

                    @Override
                    public void onSplashAdSuccessPresent() {
                        if (mImpressionListener != null) {
                            mImpressionListener.onSplashAdShow();
                        }
                    }

                    @Override
                    public void onSplashAdSuccessLoad() {
                        if (mLoadListener != null) {
                            mLoadListener.onAdCacheLoaded();
                        }

                    }

                    @Override
                    public void onSplashAdFailToLoad(WindAdError windAdError, String s) {
                        if (mLoadListener != null) {
                            mLoadListener.onAdLoadError("" + windAdError.getErrorCode(), windAdError.toString());
                        }
                    }

                    @Override
                    public void onSplashAdClicked() {
                        if (mImpressionListener != null) {
                            mImpressionListener.onSplashAdClicked();
                        }
                    }

                    @Override
                    public void onSplashClosed() {
                        if (mImpressionListener != null) {
                            mImpressionListener.onSplashAdDismiss();
                        }
                    }
                });

                mWindSplashAD.loadAdOnly();
            }
        });
    }

    @Override
    public void destory() {
        mWindSplashAD = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return SigmobATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        if (isAdReady()) {
            mWindSplashAD.showAd(container);
        }
    }
}
