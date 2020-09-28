package com.anythink.network.applovin;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.applovin.nativeAds.AppLovinNativeAd;
import com.applovin.nativeAds.AppLovinNativeAdLoadListener;
import com.applovin.sdk.AppLovinSdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/11.
 */

public class ApplovinATAdapter extends CustomNativeAdapter {
    private final String TAG = ApplovinATAdapter.class.getSimpleName();

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String sdkkey = "";
        if (serverExtras.containsKey("sdkkey")) {
            sdkkey = serverExtras.get("sdkkey").toString();
        }
        if (TextUtils.isEmpty(sdkkey)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "applovin sdkkey empty.");
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

        AppLovinSdk sdk = ApplovinATInitManager.getInstance().initSDK(context, sdkkey, serverExtras);
        loadApplovinNativeAds(context, sdk, requestNum, isAutoPlay);

    }

    public void loadApplovinNativeAds(final Context context, final AppLovinSdk sdk, int adNum, final boolean isAutoPlay) {


        sdk.getNativeAdService().loadNativeAds(adNum, new AppLovinNativeAdLoadListener() {
            @Override
            public void onNativeAdsLoaded(final List list) {
                // Native ads loaded; do something with this, e.g. render into your custom view.
                boolean isReturn = false;
                List<CustomNativeAd> resultList = new ArrayList<>();
                for (Object object : list) {
                    if (object instanceof AppLovinNativeAd) {
                        isReturn = true;
                        ApplovinATNativeAd nativeAd = new ApplovinATNativeAd(context, (AppLovinNativeAd) object, sdk);
                        nativeAd.setIsAutoPlay(isAutoPlay);
                        resultList.add(nativeAd);
                    }
                }

                if (!isReturn) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "Request success but no Ad Cache.");
                    }
                } else {
                    CustomNativeAd[] customNativeAds = new CustomNativeAd[resultList.size()];
                    customNativeAds = resultList.toArray(customNativeAds);
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded(customNativeAds);
                    }
                }

            }

            @Override
            public void onNativeAdsFailedToLoad(final int errorCode) {
                // Native ads failed to load for some reason, likely a network error.
                // Compare errorCode to the available constants in AppLovinErrorCodes.
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(errorCode + "", "");
                }

            }
        });

    }

    @Override
    public String getNetworkSDKVersion() {
        return ApplovinATConst.getNetworkVersion();
    }

    @Override
    public void destory() {

    }

    @Override
    public String getNetworkName() {
        return ApplovinATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return ApplovinATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return "";
    }
}
