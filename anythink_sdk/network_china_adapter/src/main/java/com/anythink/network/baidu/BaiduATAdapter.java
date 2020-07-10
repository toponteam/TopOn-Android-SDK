package com.anythink.network.baidu;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;
import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaiduATAdapter extends CustomNativeAdapter {

    String mAdPlaceId;

    CustomNativeListener mCustomNativeListener;

    @Override
    public void loadNativeAd(final Context context, CustomNativeListener customNativeListener, Map<String, Object> serverExtras, final Map<String, Object> localExtras) {
        String mAppId = "";
        if (serverExtras.containsKey("app_id")) {
            mAppId = serverExtras.get("app_id").toString();
        }
        if (serverExtras.containsKey("ad_place_id")) {
            mAdPlaceId = serverExtras.get("ad_place_id").toString();
        }

        mCustomNativeListener = customNativeListener;
        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
            if (mCustomNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id or ad_place_id is empty.");
                mCustomNativeListener.onNativeAdFailed(this, adError);
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
        BaiduATInitManager.getInstance().initSDK(context, serverExtras, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context, localExtras);
            }

            @Override
            public void onError(Throwable e) {
                if (mCustomNativeListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                    mCustomNativeListener.onNativeAdFailed(BaiduATAdapter.this, adError);
                }
            }
        });
    }

    private void startLoadAd(final Context context, final Map<String, Object> localExtras) {
        BaiduNative baidu = new BaiduNative(context, mAdPlaceId, new BaiduNative.BaiduNativeNetworkListener() {
            @Override
            public void onNativeFail(NativeErrorCode arg0) {
                if (mCustomNativeListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", arg0.name());
                    mCustomNativeListener.onNativeAdFailed(BaiduATAdapter.this, adError);
                }
            }

            @Override
            public void onNativeLoad(List<NativeResponse> arg0) {
                List<CustomNativeAd> customNativeAds = new ArrayList<>();
                for (NativeResponse response : arg0) {
                    BaiduATNativeAd upArpuNativeAd = new BaiduATNativeAd(context, response, mCustomNativeListener, localExtras);
                    customNativeAds.add(upArpuNativeAd);
                }

                if (mCustomNativeListener != null) {
                    mCustomNativeListener.onNativeAdLoaded(BaiduATAdapter.this, customNativeAds);
                }

            }
        });

        float density = context.getResources().getDisplayMetrics().density;

        RequestParameters requestParameters = new RequestParameters.Builder().setWidth((int) (640 * density))
                .setHeight((int) (360 * density))
                .downloadAppConfirmPolicy(
                        RequestParameters.DOWNLOAD_APP_CONFIRM_CUSTOM_BY_APP).build();
        baidu.makeRequest(requestParameters);
    }

    @Override
    public String getSDKVersion() {
        return BaiduATConst.getNetworkVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return BaiduATInitManager.getInstance().getNetworkName();
    }
}
