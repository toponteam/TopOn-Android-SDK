/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.appnext;

import android.content.Context;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppnextATAdapter extends CustomNativeAdapter {
    private final String TAG = AppnextATAdapter.class.getSimpleName();
    String mPlacementId;


    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("placement_id")) {
            mPlacementId = (String) serverExtras.get("placement_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "placement_id is empty!");
            }
            return;
        }

        AppnextATInitManager.getInstance().initSDK(context, serverExtras);

        final AppnextATNativeAd.LoadCallbackListener selfListener = new AppnextATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                synchronized (AppnextATAdapter.this) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded(nativeAd);
                    }
                }

            }

            @Override
            public void onFail(String errorCode, String errorMsg) {
                synchronized (AppnextATAdapter.this) {
                    mLoadListener.onAdLoadError(errorCode, errorMsg);
                }
            }

        };


        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    AppnextATNativeAd appnextNativeAd = new AppnextATNativeAd(context, mPlacementId, selfListener);
                    appnextNativeAd.loadAd();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }
        });

    }

    @Override
    public void destory() {

    }


    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return AppnextATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AppnextATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }
}
