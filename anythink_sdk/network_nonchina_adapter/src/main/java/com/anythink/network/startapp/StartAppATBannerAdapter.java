package com.anythink.network.startapp;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.banner.Cover;
import com.startapp.sdk.ads.banner.Mrec;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.model.AdPreferences;

import java.util.Map;

public class StartAppATBannerAdapter extends CustomBannerAdapter {

    String adTag = "";
    CustomBannerListener mListener;

    View mBannerView;

    @Override
    public void loadBannerAd(ATBannerView bannerView, Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomBannerListener customBannerListener) {
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


        mListener = customBannerListener;

        if (TextUtils.isEmpty(appId)) {
            if (mListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id could not be null.");
                mListener.onBannerAdLoadFail(this, adError);
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "context need be activity.");
                mListener.onBannerAdLoadFail(this, adError);
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
                if (mListener != null) {
                    mListener.onBannerAdLoaded(StartAppATBannerAdapter.this);
                }
            }

            @Override
            public void onFailedToReceiveAd(View banner) {
                if (mListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "StartApp Banner Load Fail");
                    mListener.onBannerAdLoadFail(StartAppATBannerAdapter.this, adError);
                }
            }

            @Override
            public void onImpression(View view) {

            }

            @Override
            public void onClick(View banner) {
                if (mListener != null) {
                    mListener.onBannerAdClicked(StartAppATBannerAdapter.this);
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
                if (mListener != null) {
                    mListener.onBannerAdLoaded(StartAppATBannerAdapter.this);
                }
            }

            @Override
            public void onFailedToReceiveAd(View banner) {
                if (mListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "StartApp Banner Load Fail");
                    mListener.onBannerAdLoadFail(StartAppATBannerAdapter.this, adError);
                }
            }

            @Override
            public void onImpression(View view) {

            }

            @Override
            public void onClick(View banner) {
                if (mListener != null) {
                    mListener.onBannerAdClicked(StartAppATBannerAdapter.this);
                }
            }
        });

        startAppBanner.loadAd();
    }


    @Override
    public String getSDKVersion() {
        return StartAppATConst.getSDKVersion();
    }

    @Override
    public void clean() {
        mBannerView = null;
    }

    @Override
    public String getNetworkName() {
        return StartAppATInitManager.getInstance().getNetworkName();
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }
}
