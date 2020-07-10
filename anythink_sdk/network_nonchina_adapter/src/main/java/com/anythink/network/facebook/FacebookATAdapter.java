package com.anythink.network.facebook;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.BaseNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.facebook.ads.Ad;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeBannerAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/12.
 */

public class FacebookATAdapter extends CustomNativeAdapter {

    String mPayload;
    String unitId = "";
    String unitType = "";
    String unitHeight = "";
    boolean isAutoPlay = false;

    @Override
    public void loadNativeAd(final Context context, final CustomNativeListener customNativeListener, final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {
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
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "facebook unitId is empty.");
                customNativeListener.onNativeAdFailed(this, adError);
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


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    startAdLoad(context, customNativeListener);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (customNativeListener != null) {
                        AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                        customNativeListener.onNativeAdFailed(FacebookATAdapter.this, adError);
                    }
                }
            }
        }).start();
    }


    private void startAdLoad(final Context context, final CustomNativeListener customNativeListener) {
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

            }

            @Override
            public void onError(Ad ad, com.facebook.ads.AdError adError) {
                com.anythink.core.api.AdError adUpError = ErrorCode.getErrorCode(ErrorCode.noADError, adError.getErrorCode() + "", adError.getErrorMessage());
                if (customNativeListener != null) {
                    customNativeListener.onNativeAdFailed(FacebookATAdapter.this, adUpError);
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                switch (unitType) {
                    case "1":
                        FacebookATNativeBannerAd nativeBanner = new FacebookATNativeBannerAd(context, (NativeBannerAd) ad, unitHeight);
                        List<CustomNativeAd> baseNativeBannerAdList = new ArrayList<>();
                        baseNativeBannerAdList.add(nativeBanner);
                        if (customNativeListener != null) {
                            customNativeListener.onNativeAdLoaded(FacebookATAdapter.this, baseNativeBannerAdList);
                        }
                        break;
                    default:
                        FacebookATNativeAd nativeAd = new FacebookATNativeAd(context, (NativeAd) ad);
                        List<CustomNativeAd> baseNativeAdList = new ArrayList<>();
                        baseNativeAdList.add(nativeAd);
                        if (customNativeListener != null) {
                            customNativeListener.onNativeAdLoaded(FacebookATAdapter.this, baseNativeAdList);
                        }
                        break;
                }

            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };

        switch (unitType) {
            case "1":
                NativeBannerAd nativeBanner = new NativeBannerAd(context, unitId);
                nativeBanner.setAdListener(nativeAdListener);
                nativeBanner.loadAd();

                break;
            default:
                NativeAd nativeAd = new NativeAd(context, unitId);
                nativeAd.setAdListener(nativeAdListener);
                nativeAd.loadAd();
                break;
        }
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return FacebookATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getSDKVersion() {
        return FacebookATConst.getNetworkVersion();
    }
}
