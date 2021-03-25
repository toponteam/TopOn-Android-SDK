/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.huawei;

import android.content.Context;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.Map;

public class HuaweiATAdapter extends CustomNativeAdapter {
    String mAdId;

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        if (serverExtras.containsKey("ad_id")) {
            mAdId = (String) serverExtras.get("ad_id");

        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "AdId is empty.");
            }
            return;
        }

        HuaweiATInitManager.getInstance().initSDK(context, serverExtras, new HuaweiATInitManager.InitListener() {
            @Override
            public void onSuccess() {
                startLoadAd(context, serverExtras);
            }
        });
    }

    private void startLoadAd(Context context, Map<String, Object> serverExtras) {
        HuaweiATNativeAd huaweiATNativeAd = new HuaweiATNativeAd(context, mAdId);
        huaweiATNativeAd.loadAd(serverExtras, new HuaweiATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd customNativeAd) {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded(customNativeAd);
                }
            }

            @Override
            public void onFail(String errorCode, String errorMsg) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(errorCode, errorMsg);
                }
            }
        });
    }

    @Override
    public void destory() {

    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return HuaweiATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return HuaweiATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return HuaweiATInitManager.getInstance().getNetworkName();
    }
}
