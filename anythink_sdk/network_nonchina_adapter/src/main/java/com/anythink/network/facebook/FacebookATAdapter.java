/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.facebook;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.MediationBidManager;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeBannerAd;

import java.util.Map;


public class FacebookATAdapter extends CustomNativeAdapter {

    String mPayload;
    String unitId = "";
    String unitType = "";
    String unitHeight = "";
    boolean isAutoPlay = false;

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {
        try {
            if (serverExtras.containsKey("unit_id")) {
                unitId = serverExtras.get("unit_id").toString();
            }

            if (serverExtras.containsKey("unit_type")) {
                unitType = serverExtras.get("unit_type").toString();
            }

            if (serverExtras.containsKey("height")) {
                unitHeight = serverExtras.get("height").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "facebook unitId is empty.");
            }
            return;
        }


        try {
            if (serverExtras != null) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }

        FacebookATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras);

        if (serverExtras.containsKey("payload")) {
            mPayload = serverExtras.get("payload").toString();
        }


        startAdLoad(context);

    }


    private void startAdLoad(final Context context) {
        switch (unitType) {
            case "1":
                NativeBannerAd nativeBanner = new NativeBannerAd(context, unitId);
                final FacebookATNativeBannerAd facebookATNativeBannerAd = new FacebookATNativeBannerAd(context, nativeBanner, unitHeight);
                facebookATNativeBannerAd.loadAd(mPayload, new FacebookATNativeBannerAd.FBNativeBannerLoadListener() {
                    @Override
                    public void onLoadSuccess() {
                        if (mLoadListener != null) {
                            if (mLoadListener != null) {
                                mLoadListener.onAdCacheLoaded(facebookATNativeBannerAd);
                            }
                        }
                    }

                    @Override
                    public void onLoadFail(String code, String message) {
                        if (mLoadListener != null) {
                            mLoadListener.onAdLoadError(code, message);
                        }
                    }
                });
                break;
            default:
                NativeAd nativeAd = new NativeAd(context, unitId);
                final FacebookATNativeAd facebookATNativeAd = new FacebookATNativeAd(context, nativeAd);
                facebookATNativeAd.loadAd(mPayload, new FacebookATNativeAd.FBNativeLoadListener() {
                    @Override
                    public void onLoadSuccess() {
                        if (mLoadListener != null) {
                            if (mLoadListener != null) {
                                mLoadListener.onAdCacheLoaded(facebookATNativeAd);
                            }
                        }
                    }

                    @Override
                    public void onLoadFail(String code, String message) {
                        if (mLoadListener != null) {
                            mLoadListener.onAdLoadError(code, message);
                        }
                    }
                });
                break;
        }
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return FacebookATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public String getNetworkPlacementId() {
        return unitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return FacebookATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public MediationBidManager getBidManager() {
        return FacebookBidkitManager.getInstance();
    }
}
