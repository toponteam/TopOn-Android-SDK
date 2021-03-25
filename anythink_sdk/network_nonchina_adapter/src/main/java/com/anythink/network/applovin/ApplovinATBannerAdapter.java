/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.applovin;

import android.content.Context;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.applovin.adview.AppLovinAdView;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;

import java.util.Map;

/**
 * Created by Z on 2018/6/27.
 */

public class ApplovinATBannerAdapter extends CustomBannerAdapter {
    private static final String TAG = ApplovinATBannerAdapter.class.getSimpleName();


    String sdkkey = "", zoneid = "";

    String size = "";


    AppLovinAdView mBannerView;

    /***
     * init and load
     *
     */
    private void initAndLoad(Context activity, Map<String, Object> serverExtras) {

        AppLovinSdk applovinSdk = ApplovinATInitManager.getInstance().initSDK(activity, sdkkey, serverExtras);

        AppLovinAdView appLovinAdView = null;
        switch (size) {
            case "320x50":
                appLovinAdView = new AppLovinAdView(applovinSdk, AppLovinAdSize.BANNER, activity);
                break;
            case "300x250":
                appLovinAdView = new AppLovinAdView(applovinSdk, AppLovinAdSize.MREC, activity);
                break;
            default:
                appLovinAdView = new AppLovinAdView(applovinSdk, AppLovinAdSize.BANNER, activity);
                break;
        }

        final AppLovinAdView finalAdView = appLovinAdView;


        finalAdView.setAdDisplayListener(new AppLovinAdDisplayListener() {
            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }

            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
            }
        });

        finalAdView.setAdClickListener(new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }
        });


        AppLovinAdLoadListener adLoadListener = new AppLovinAdLoadListener() {
            public void adReceived(final AppLovinAd ad) {
                finalAdView.renderAd(ad);
                mBannerView = finalAdView;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void failedToReceiveAd(int i) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(i + "", "");
                }
            }
        };

        applovinSdk.getAdService().loadNextAdForZoneId(zoneid, adLoadListener);

    }


    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public void destory() {
        if (mBannerView != null) {
            mBannerView.setAdLoadListener(null);
            mBannerView.setAdClickListener(null);
            mBannerView.setAdDisplayListener(null);
            mBannerView.destroy();
            mBannerView = null;
        }
    }


    @Override
    public void loadCustomNetworkAd(Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        if (serverExtras.containsKey("sdkkey") && serverExtras.containsKey("zone_id")) {
            sdkkey = (String) serverExtras.get("sdkkey");
            zoneid = (String) serverExtras.get("zone_id");
        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "sdkkey or zone_id is empty!");
            }
            return;
        }

        if (serverExtras.containsKey("size")) {
            size = serverExtras.get("size").toString();
        }

        initAndLoad(activity, serverExtras);

    }


    @Override
    public String getNetworkSDKVersion() {
        return ApplovinATInitManager.getInstance().getNetworkVersion();
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
        return zoneid;
    }
}