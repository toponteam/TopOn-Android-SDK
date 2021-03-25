/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.admob;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.Map;

public class AdmobATAdapter extends CustomNativeAdapter {

    private String mUnitId;

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, final Map<String, Object> localExtras) {
        String appid = "";
        String unitId = "";
        String mediaRatio = "";

        if (serverExtras.containsKey("app_id")) {
            appid = serverExtras.get("app_id").toString();
        }
        if (serverExtras.containsKey("unit_id")) {
            unitId = serverExtras.get("unit_id").toString();
        }

        if (serverExtras.containsKey("media_ratio")) {
            mediaRatio = serverExtras.get("media_ratio").toString();
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "appid or unitId is empty.");

            }
            return;
        }

        mUnitId = unitId;

//        int requestNum = 1;
//        try {
//            if (serverExtras != null && serverExtras.containsKey(CustomNativeAd.AD_REQUEST_NUM)) {
//                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        boolean isAutoPlay = false;
        try {
            if (serverExtras != null && serverExtras.containsKey(CustomNativeAd.IS_AUTO_PLAY_KEY)) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }


        final String finalUnitId = unitId;
        final String finalMediaRatio = mediaRatio;
        final boolean finalIsAutoPlay = isAutoPlay;
        AdMobATInitManager.getInstance().initSDK(context, serverExtras, new AdMobATInitManager.InitListener() {
            @Override
            public void initSuccess() {
                startLoadAd(context,
                        localExtras,
                        finalUnitId,
                        finalMediaRatio,
                        finalIsAutoPlay);
            }
        });
    }

    private void startLoadAd(Context context, Map<String, Object> localExtras, String unitId, String mediaRatio, boolean isAutoPlay) {
        Bundle persionalBundle = AdMobATInitManager.getInstance().getRequestBundle(context);


        AdmobATNativeAd.LoadCallbackListener selfListener = new AdmobATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded(nativeAd);
                }
            }

            @Override
            public void onFail(String errorCode, String errorMsg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(errorCode, errorMsg);
                }
            }
        };

        AdmobATNativeAd admobiATNativeAd = new AdmobATNativeAd(context, mediaRatio, unitId, selfListener, localExtras);
        admobiATNativeAd.setIsAutoPlay(isAutoPlay);
        admobiATNativeAd.loadAd(context, persionalBundle);
    }

    @Override
    public String getNetworkSDKVersion() {
        return AdMobATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AdMobATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }
}

