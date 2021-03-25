/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.mintegral;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.mbridge.msdk.out.MBSplashHandler;
import com.mbridge.msdk.out.MBSplashLoadListener;
import com.mbridge.msdk.out.MBSplashShowListener;

import java.util.Map;


public class MintegralATSplashAdapter extends CustomSplashAdapter {

    private static final String TAG = MintegralATSplashAdapter.class.getSimpleName();
    String mPayload;
    String mCustomData = "{}";
    int countdown = 5;
    int orientation = Configuration.ORIENTATION_PORTRAIT;
    boolean allowSkip = true;

    String appid = "";
    String unitId = "";
    String sdkKey = "";
    String placementId = "";

    @Override
    public String getNetworkSDKVersion() {
        return MintegralATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        try {
            if (serverExtras.containsKey("appid")) {
                appid = serverExtras.get("appid").toString();
            }
            if (serverExtras.containsKey("unitid")) {
                unitId = serverExtras.get("unitid").toString();
            }

            if (serverExtras.containsKey("placement_id")) {
                placementId = serverExtras.get("placement_id").toString();
            }
            if (serverExtras.containsKey("appkey")) {
                sdkKey = serverExtras.get("appkey").toString();
            }

            if (serverExtras.containsKey("payload")) {
                mPayload = serverExtras.get("payload").toString();
            }

            if (serverExtras.containsKey("tp_info")) {
                mCustomData = serverExtras.get("tp_info").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId) || TextUtils.isEmpty(sdkKey)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "mintegral appid ,unitid or sdkkey is empty.");
            }
            return;
        }

        if (serverExtras.containsKey("countdown")) {
            countdown = Integer.parseInt(serverExtras.get("countdown").toString());
        }

        if (serverExtras.containsKey("allows_skip")) {
            allowSkip = Integer.parseInt(serverExtras.get("allows_skip").toString()) == 1;
        }

        if (serverExtras.containsKey("orientation")) {
            orientation = TextUtils.equals(serverExtras.get("orientation").toString(), "2") ? Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
        }


        MintegralATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras, new MintegralATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoad();
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", e.getMessage());
                }
            }
        });
    }

    MBSplashHandler splashHandler = null;

    private void startLoad() {
        splashHandler = new MBSplashHandler(placementId, unitId, allowSkip, countdown, orientation, 0, 0);
        splashHandler.setLoadTimeOut(mFetchAdTimeout / 1000);//unit: second
        splashHandler.setSplashLoadListener(new MBSplashLoadListener() {
            @Override
            public void onLoadSuccessed(int i) {

                if (splashHandler != null && splashHandler.isReady()) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }

                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "Mintegral Splash Ad is not ready.");
                    }
                }

            }

            @Override
            public void onLoadFailed(String s, int i) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", s);
                }
            }
        });

        splashHandler.setSplashShowListener(new MBSplashShowListener() {
            @Override
            public void onShowSuccessed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdShow();
                }
            }

            @Override
            public void onShowFailed(String s) {

            }

            @Override
            public void onAdClicked() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdClicked();
                }
            }

            @Override
            public void onDismiss(int i) {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }

            @Override
            public void onAdTick(long l) {

            }
        });


        splashHandler.preLoad();
        splashHandler.onResume();
    }

    @Override
    public boolean isAdReady() {
        return splashHandler != null && splashHandler.isReady();
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        if (splashHandler != null) {
            splashHandler.show(container);
        }
    }


    @Override
    public void destory() {
        if (splashHandler != null) {
            splashHandler.onPause();
            splashHandler.onDestroy();
        }

    }

    @Override
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return MintegralATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return unitId;
    }
}
