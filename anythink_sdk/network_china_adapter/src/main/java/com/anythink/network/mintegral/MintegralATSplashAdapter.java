/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.mintegral;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.mintegral.msdk.out.MTGSplashHandler;
import com.mintegral.msdk.out.MTGSplashLoadListener;
import com.mintegral.msdk.out.MTGSplashShowListener;

import java.util.Map;


/**
 * @author Z
 */

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

    MTGSplashHandler splashHandler = null;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        try {

            //支持视频
            boolean suportVideo = false;
            try {
                if (serverExtra.containsKey("appid")) {
                    appid = serverExtra.get("appid").toString();
                }
                if (serverExtra.containsKey("unitid")) {
                    unitId = serverExtra.get("unitid").toString();
                }

                if (serverExtra.containsKey("placement_id")) {
                    placementId = serverExtra.get("placement_id").toString();
                }
                if (serverExtra.containsKey("appkey")) {
                    sdkKey = serverExtra.get("appkey").toString();
                }

                if (serverExtra.containsKey("payload")) {
                    mPayload = serverExtra.get("payload").toString();
                }

                if (serverExtra.containsKey("tp_info")) {
                    mCustomData = serverExtra.get("tp_info").toString();
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

            if (serverExtra.containsKey("countdown")) {
                countdown = Integer.parseInt(serverExtra.get("countdown").toString());
            }

            if (serverExtra.containsKey("allows_skip")) {
                allowSkip = Integer.parseInt(serverExtra.get("allows_skip").toString()) == 1;
            }

            if (serverExtra.containsKey("orientation")) {
                orientation = TextUtils.equals(serverExtra.get("orientation").toString(), "2") ? Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
            }


            MintegralATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtra, new MintegralATInitManager.InitCallback() {
                @Override
                public void onSuccess() {
                    startLoad(mContainer);
                }

                @Override
                public void onError(Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", e.getMessage());
            }
        }
    }

    private void startLoad(final ViewGroup container) {
        splashHandler = new MTGSplashHandler(placementId, unitId, allowSkip, countdown, orientation, 0, 0);
        splashHandler.setLoadTimeOut(5);//unit: second
        splashHandler.setSplashLoadListener(new MTGSplashLoadListener() {
            @Override
            public void onLoadSuccessed(int i) {
                if (splashHandler != null && splashHandler.isReady()) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                    splashHandler.show(container);

                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "Mintegral Splash Ad is not ready.");
                    }
                }

            }

            @Override
            public void onLoadFailed(String s, int i) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(i + "", s);
                }
            }
        });

        splashHandler.setSplashShowListener(new MTGSplashShowListener() {
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
    public String getNetworkName() {
        return MintegralATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        if (splashHandler != null) {
            splashHandler.onPause();
            splashHandler.onDestroy();
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return unitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return MintegralATConst.getNetworkVersion();
    }
}
