package com.anythink.network.flurry;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/18.
 */

public class FlurryATAdapter extends CustomNativeAdapter {

    private final static String TAG = FlurryATAdapter.class.getSimpleName();
    int mCallbackCount;

    private String adSpace;

    @Override
    public void loadCustomNetworkAd(final Context context
            , final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {

        String sdkKey = "";
        adSpace = "";
        try {
            if (serverExtras.containsKey("sdk_key")) {
                sdkKey = serverExtras.get("sdk_key").toString();
            }
            if (serverExtras.containsKey("ad_space")) {
                adSpace = serverExtras.get("ad_space").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(sdkKey) || TextUtils.isEmpty(adSpace)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "flurry sdkkey or adspace is empty.");
            }
            return;
        }

        final List<CustomNativeAd> adList = new ArrayList<>();

        int requestNum = 1;
        try {
            if (serverExtras != null) {
                requestNum = Integer.parseInt(serverExtras.get(CustomNativeAd.AD_REQUEST_NUM).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean isAutoPlay = false;
        try {
            if (serverExtras != null) {
                isAutoPlay = Boolean.parseBoolean(serverExtras.get(CustomNativeAd.IS_AUTO_PLAY_KEY).toString());
            }
        } catch (Exception e) {

        }

        final int finalRequestNum = requestNum;
        final boolean finalIsAutoPlay = isAutoPlay;

        final FlurryATNativeAd.LoadCallbackListener selfListener = new FlurryATNativeAd.LoadCallbackListener() {
            @Override
            public void onSuccess(CustomNativeAd nativeAd) {
                synchronized (FlurryATAdapter.this) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded(nativeAd);
                    }
                }
            }

            @Override
            public void onFail(String errorCode, String errorMsg) {
                synchronized (FlurryATAdapter.this) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError(errorCode, errorMsg);
                    }
                }
            }

        };


        final String tmpSapce = adSpace;
        FlurryATInitManager.getInstance().initSDK(context, serverExtras);
        FlurryATInitManager.getInstance().postDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    FlurryATNativeAd flurryNativeAd = new FlurryATNativeAd(context, selfListener, tmpSapce, localExtras);
                    flurryNativeAd.setIsAutoPlay(finalIsAutoPlay);
                    flurryNativeAd.loadAd();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
            }
        }, 5000);


    }

    @Override
    public String getNetworkSDKVersion() {
        return FlurryATConst.getNetworkVersion();
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return FlurryATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return FlurryATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return adSpace;
    }
}
