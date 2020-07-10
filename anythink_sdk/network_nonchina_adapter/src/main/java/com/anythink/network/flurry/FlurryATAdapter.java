package com.anythink.network.flurry;

import android.content.Context;
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
 * Created by Z on 2018/1/18.
 */

public class FlurryATAdapter extends CustomNativeAdapter {

    private final static String TAG = FlurryATAdapter.class.getSimpleName();
    int mCallbackCount;

    List<CustomNativeAd> adList = new ArrayList<>();

    @Override
    public void loadNativeAd(final Context context, final CustomNativeListener customNativeListener
            , final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {

        String sdkKey = "";
        String adSpace = "";
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
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "flurry sdkkey or adspace is empty.");
                customNativeListener.onNativeAdFailed(this, adError);
            }
            return;
        }

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
                    mCallbackCount++;
                    adList.add(nativeAd);
                    finishLoad(null);
                }
            }

            @Override
            public void onFail(AdError error) {
                synchronized (FlurryATAdapter.this) {
                    mCallbackCount++;
                    finishLoad(error);
                }
            }

            private void finishLoad(AdError adError) {
                if (mCallbackCount >= finalRequestNum) {
                    if (adList.size() > 0) {
                        if (customNativeListener != null) {
                            customNativeListener.onNativeAdLoaded(FlurryATAdapter.this, adList);
                        }
                    } else {
                        if (mCallbackCount >= finalRequestNum) {
                            if (adError == null) {
                                adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "");
                            }
                            customNativeListener.onNativeAdFailed(FlurryATAdapter.this, adError);
                        }
                    }
                }
            }
        };

        try {
            final String tmpSapce = adSpace;
            FlurryATInitManager.getInstance().initSDK(context, serverExtras);
            FlurryATInitManager.getInstance().postDelay(new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < finalRequestNum; i++) {
                        FlurryATNativeAd flurryNativeAd = new FlurryATNativeAd(context, selfListener, tmpSapce, localExtras);
                        flurryNativeAd.setIsAutoPlay(finalIsAutoPlay);
                        flurryNativeAd.loadAd();
                    }

                }
            }, 5000);


        } catch (Exception e) {
            e.printStackTrace();
            if (customNativeListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", e.getMessage());
                customNativeListener.onNativeAdFailed(this, adError);
            }
        }
    }

    @Override
    public String getSDKVersion() {
        return FlurryATConst.getNetworkVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return FlurryATInitManager.getInstance().getNetworkName();
    }
}
