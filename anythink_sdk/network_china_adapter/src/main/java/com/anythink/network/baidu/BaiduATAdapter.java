package com.anythink.network.baidu;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaiduATAdapter extends CustomNativeAdapter {

    String mAdPlaceId;

    private void startLoadAd(final Context context) {
        BaiduNative baidu = new BaiduNative(context, mAdPlaceId, new BaiduNative.BaiduNativeNetworkListener() {
            @Override
            public void onNativeFail(NativeErrorCode arg0) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", arg0.name());
                }
            }

            @Override
            public void onNativeLoad(List<NativeResponse> arg0) {
                List<CustomNativeAd> resultList = new ArrayList<>();
                for (NativeResponse response : arg0) {
                    BaiduATNativeAd upArpuNativeAd = new BaiduATNativeAd(context, response);
                    resultList.add(upArpuNativeAd);
                }

                CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                customNativeAds = resultList.toArray(customNativeAds);
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded(customNativeAds);
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
    public String getNetworkName() {
        return BaiduATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String mAppId = "";
        if (serverExtra.containsKey("app_id")) {
            mAppId = serverExtra.get("app_id").toString();
        }
        if (serverExtra.containsKey("ad_place_id")) {
            mAdPlaceId = serverExtra.get("ad_place_id").toString();
        }

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAdPlaceId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id or ad_place_id is empty.");
            }
            return;
        }

        int requestNum = 1;
        try {
            if (serverExtra.containsKey(CustomNativeAd.AD_REQUEST_NUM)) {
                requestNum = Integer.parseInt(serverExtra.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
        }

        boolean isAutoPlay = false;
        try {
            if (serverExtra != null && serverExtra.containsKey(CustomNativeAd.IS_AUTO_PLAY_KEY)) {
                isAutoPlay = Boolean.parseBoolean(serverExtra.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }

        final int finalRequestNum = requestNum;
        BaiduATInitManager.getInstance().initSDK(context, serverExtra, new BaiduATInitManager.InitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
            }

            @Override
            public void onError(Throwable e) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", e.getMessage());
                }
            }
        });
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkPlacementId() {
        return mAdPlaceId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return BaiduATConst.getNetworkVersion();
    }
}
