package com.anythink.network.startapp;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.banner.Mrec;
import com.startapp.sdk.adsbase.model.AdPreferences;

import java.util.Map;

public class StartAppATBannerAdapter extends CustomBannerAdapter {

    String adTag = "";

    View mBannerView;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        String appId = "";
        String size = "";
        if (serverExtras.containsKey("app_id")) {
            appId = serverExtras.get("app_id").toString();
        }
        if (serverExtras.containsKey("ad_tag")) {
            adTag = serverExtras.get("ad_tag").toString();
        }
        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }


        if (TextUtils.isEmpty(appId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id could not be null.");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "context need be activity.");
            }
            return;
        }


        StartAppATInitManager.getInstance().initSDK(context, serverExtras);
        AdPreferences adPreferences = new AdPreferences();
        if (!TextUtils.isEmpty(adTag)) {
            adPreferences.setAdTag(adTag);
        }


        switch (size) {
            case "320x50":
                requestBanner320x50((Activity) context, adPreferences);
                break;
            case "300x250":
                requestBanner300x250((Activity) context, adPreferences);
                break;
            default:
                requestBanner320x50((Activity) context, adPreferences);
                break;
        }


    }

    private void requestBanner320x50(Activity context, AdPreferences adPreferences) {
        Banner startAppBanner = new Banner(context, adPreferences, new BannerListener() {
            @Override
            public void onReceiveAd(View banner) {
                mBannerView = banner;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onFailedToReceiveAd(View banner) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "StartApp Banner onFailedToReceiveAd");
                }
            }

            @Override
            public void onImpression(View view) {

            }

            @Override
            public void onClick(View banner) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }
        });

        startAppBanner.loadAd();
    }

    private void requestBanner300x250(Activity context, AdPreferences adPreferences) {
        Mrec startAppBanner = new Mrec(context, adPreferences, new BannerListener() {
            @Override
            public void onReceiveAd(View banner) {
                mBannerView = banner;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onFailedToReceiveAd(View banner) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "StartApp Banner onFailedToReceiveAd");
                }
            }

            @Override
            public void onImpression(View view) {

            }

            @Override
            public void onClick(View banner) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }
        });

        startAppBanner.loadAd();
    }


    @Override
    public String getNetworkSDKVersion() {
        return StartAppATConst.getSDKVersion();
    }

    @Override
    public void destory() {
        if (mBannerView != null) {
            if (mBannerView instanceof Banner) {
                ((Banner) mBannerView).setBannerListener(null);
            } else if (mBannerView instanceof Mrec) {
                ((Mrec) mBannerView).setBannerListener(null);
            }
            mBannerView = null;
        }
    }

    @Override
    public String getNetworkName() {
        return StartAppATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public String getNetworkPlacementId() {
        return adTag;
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }
}
