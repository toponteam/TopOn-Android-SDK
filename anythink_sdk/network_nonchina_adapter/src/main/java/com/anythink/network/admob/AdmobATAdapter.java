package com.anythink.network.admob;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.Map;

/**
 * Created by zhou on 2018/1/16.
 */

public class AdmobATAdapter extends CustomNativeAdapter {

    private String mUnitId;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
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


        AdMobATInitManager.getInstance().initSDK(context, serverExtras);

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
        return AdmobATConst.getNetworkVersion();
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

