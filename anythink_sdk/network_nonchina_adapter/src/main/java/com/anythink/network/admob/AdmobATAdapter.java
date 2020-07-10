package com.anythink.network.admob;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhou on 2018/1/16.
 */

public class AdmobATAdapter extends CustomNativeAdapter {

    private final String TAG = AdmobATAdapter.class.getSimpleName();
    int mCallbackCount;

    List<CustomNativeAd> mAdList = new ArrayList<>();

    @Override
    public void loadNativeAd(Context context, final CustomNativeListener customNativeListener, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
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
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "admobi appid or unitId is empty.");
                customNativeListener.onNativeAdFailed(AdmobATAdapter.this, adError);

            }
            return;
        }


        int requestNum = 1;
        try {
            if (serverExtras != null && serverExtras.containsKey(CustomNativeAd.AD_REQUEST_NUM)) {
                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean isAutoPlay = false;
        try {
            if (serverExtras != null && serverExtras.containsKey(CustomNativeAd.IS_AUTO_PLAY_KEY)) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }

        final int finalRequestNum = requestNum;


        AdMobATInitManager.getInstance().initSDK(context, serverExtras);

        Bundle persionalBundle = AdMobATInitManager.getInstance().getRequestBundle(context);


        AdmobATNativeAd.LoadCallbackListener selfListener = new AdmobATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                synchronized (AdmobATAdapter.this) {
                    mCallbackCount++;
                    mAdList.add(nativeAd);
                    finishLoad(null);
                }

            }

            @Override
            public void onFail( AdError error) {
                synchronized (AdmobATAdapter.this) {
                    mCallbackCount++;
                    finishLoad(error);

                }
            }

            private void finishLoad(AdError adError) {
                synchronized (AdmobATAdapter.this) {
                    if (mCallbackCount >= finalRequestNum) {
                        if (mAdList.size() > 0) {
                            if (customNativeListener != null) {
                                customNativeListener.onNativeAdLoaded(AdmobATAdapter.this, mAdList);
                            }
                        } else {
                            if (mCallbackCount >= finalRequestNum) {
                                if (adError == null) {
                                    adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "");
                                }
                                customNativeListener.onNativeAdFailed(AdmobATAdapter.this, adError);
                            }
                        }
                    }
                }

            }
        };


        for (int i = 0; i < requestNum; i++) {
            AdmobATNativeAd admobiATNativeAd = new AdmobATNativeAd(context, mediaRatio, unitId, selfListener, localExtras);
            admobiATNativeAd.setIsAutoPlay(isAutoPlay);
            admobiATNativeAd.loadAd(context, persionalBundle);
        }

    }

    @Override
    public String getSDKVersion() {
        return AdmobATConst.getNetworkVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return AdMobATInitManager.getInstance().getNetworkName();
    }
}

