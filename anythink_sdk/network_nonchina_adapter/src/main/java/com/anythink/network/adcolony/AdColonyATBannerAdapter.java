/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.adcolony;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyAdSize;
import com.adcolony.sdk.AdColonyAdView;
import com.adcolony.sdk.AdColonyAdViewListener;
import com.adcolony.sdk.AdColonyZone;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;

import org.json.JSONArray;

import java.util.Map;

public class AdColonyATBannerAdapter extends CustomBannerAdapter {
    private final String TAG = AdColonyATBannerAdapter.class.getSimpleName();
    String mZoneId;
    String[] mZoneIds;
    String mSize;

    AdColonyAdView adColonyAdView;

    @Override
    public void loadCustomNetworkAd(Context activity, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String mAppId = "";
        if (serverExtras != null) {
            mAppId = (String) serverExtras.get("app_id");
            mZoneId = (String) serverExtras.get("zone_id");
            String zoneIds = serverExtras.get("zone_ids").toString();
            try {
                JSONArray jsonArray = new JSONArray(zoneIds);
                mZoneIds = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    mZoneIds[i] = jsonArray.optString(i);
                }
            } catch (Exception e) {

            }

            if (serverExtras.containsKey("size")) {
                mSize = serverExtras.get("size").toString();
            }
        }


        if (!(activity instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "context must be activity!");
            }
            return;
        }

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mZoneId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " appid & mZoneId is empty.");
            }
            return;
        }

        /**
         * Configure AdColony in your launching Activity's onCreate() method so that cached ads can
         * be available as soon as possible.
         */
        AdColonyATInitManager.getInstance().initSDK(activity, serverExtras);

        AdColonyAdOptions adOptions = new AdColonyAdOptions();
        adOptions.enableConfirmationDialog(false)
                .enableResultsDialog(false);

        AdColonyAdViewListener listener = new AdColonyAdViewListener() {
            @Override
            public void onRequestFilled(AdColonyAdView ad) {
                /** Add this ad object to whatever layout you have set up for this placement */
                adColonyAdView = ad;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            public void onOpened(AdColonyAdView ad) {
            }

            public void onClosed(AdColonyAdView ad) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }


            public void onLeftApplication(AdColonyAdView ad) {
            }

            public void onClicked(AdColonyAdView ad) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

            public void onRequestNotFilled(AdColonyZone zone) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "onRequestNotFilled!");
                }
            }
        };

        AdColonyAdSize adColonyAdSize = AdColonyAdSize.BANNER;
        if (!TextUtils.isEmpty(mSize)) {
            switch (mSize) {
                case "320x50":
                    adColonyAdSize = AdColonyAdSize.BANNER;
                    break;
                case "300x250":
                    adColonyAdSize = AdColonyAdSize.MEDIUM_RECTANGLE;
                    break;
                case "728x90":
                    adColonyAdSize = AdColonyAdSize.LEADERBOARD;
                    break;
                case "160x600":
                    adColonyAdSize = AdColonyAdSize.SKYSCRAPER;
                    break;
            }
        }


        AdColony.requestAdView(mZoneId, listener, adColonyAdSize);
    }


    @Override
    public String getNetworkSDKVersion() {
        return AdColonyATInitManager.getInstance().getNetworkVersion();
    }


    @Override
    public void destory() {
        AdColony.clearCustomMessageListeners();
        if (adColonyAdView != null) {
            adColonyAdView.setListener(null);
            adColonyAdView.destroy();
            adColonyAdView = null;
        }
    }

    @Override
    public String getNetworkName() {
        return AdColonyATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AdColonyATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkPlacementId() {
        return mZoneId;
    }


    @Override
    public View getBannerView() {
        return adColonyAdView;
    }

    @Override
    public boolean supportImpressionCallback() {
        return false;
    }
}
