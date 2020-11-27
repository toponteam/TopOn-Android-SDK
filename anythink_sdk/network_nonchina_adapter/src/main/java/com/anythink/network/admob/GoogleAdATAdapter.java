package com.anythink.network.admob;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.Map;

/**
 * Native Adapter for Google Ad Manager
 */

public class GoogleAdATAdapter extends CustomNativeAdapter {

    private String mUnitId;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        String unitId = "";
        String mediaRatio = "";

        if (serverExtras.containsKey("unit_id")) {
            unitId = serverExtras.get("unit_id").toString();
        }

        if (serverExtras.containsKey("media_ratio")) {
            mediaRatio = serverExtras.get("media_ratio").toString();
        }

        if (TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "unitId is empty.");

            }
            return;
        }

        mUnitId = unitId;

        boolean isAutoPlay = false;
        try {
            if (serverExtras != null && serverExtras.containsKey(CustomNativeAd.IS_AUTO_PLAY_KEY)) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }


        GoogleAdATNativeAd.LoadCallbackListener selfListener = new GoogleAdATNativeAd.LoadCallbackListener() {
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

        GoogleAdATNativeAd googleAdATNativeAd = new GoogleAdATNativeAd(context, mediaRatio, unitId, selfListener, localExtras);
        googleAdATNativeAd.setIsAutoPlay(isAutoPlay);
        googleAdATNativeAd.loadAd(context);

    }

    @Override
    public String getNetworkSDKVersion() {
        return AdmobATConst.getNetworkVersion();
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getGoogleAdManagerName();
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

