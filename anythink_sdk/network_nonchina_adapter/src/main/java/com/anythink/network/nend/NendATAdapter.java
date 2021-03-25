/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.nend;

import android.content.Context;

import com.anythink.core.common.base.Const;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NendATAdapter extends CustomNativeAdapter {

    String mApiKey;
    int mSpotId;
    int mNativeType;


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("api_key") && serverExtras.containsKey("spot_id")) {
            mApiKey = (String) serverExtras.get("api_key");
            mSpotId = Integer.parseInt((String) serverExtras.get("spot_id"));

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id or slot_id is empty!");
            }
            return;
        }

        if (serverExtras.containsKey("is_video")) {
            mNativeType = Integer.parseInt(serverExtras.get("is_video").toString());
        }

        NendATNativeAd.LoadCallbackListener selfListener = new NendATNativeAd.LoadCallbackListener() {
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

        NendATNativeAd nendNativeAd = new NendATNativeAd(context, mApiKey, mSpotId, mNativeType, selfListener);
        nendNativeAd.loadAd();
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return NendATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public String getNetworkPlacementId() {
        try {
            return String.valueOf(mSpotId);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
