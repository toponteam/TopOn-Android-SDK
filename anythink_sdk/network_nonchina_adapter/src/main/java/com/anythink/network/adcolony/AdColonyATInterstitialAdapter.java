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

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;

import org.json.JSONArray;

import java.util.Map;

public class AdColonyATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = AdColonyATInterstitialAdapter.class.getSimpleName();
    String mZoneId;
    String[] mZoneIds;

    AdColonyInterstitial mAd;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
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
        AdColonyATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtras);

        AdColonyAdOptions adOptions = new AdColonyAdOptions();
        adOptions.enableConfirmationDialog(false)
                .enableResultsDialog(false);


        /**
         * Set up listener for interstitial ad callbacks. You only need to implement the callbacks
         * that you care about. The only required callback is onRequestFilled, as this is the only
         * way to get an ad object.
         */
        AdColonyInterstitialListener listener = new AdColonyInterstitialListener() {
            /**
             * Ad passed back in request filled callback, ad can now be shown
             */
            @Override
            public void onRequestFilled(AdColonyInterstitial ad) {
                mAd = ad;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            /** Ad request was not filled */
            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "No Fill!");
                }
            }

            /** Ad opened, reset UI to reflect state change */
            @Override
            public void onOpened(AdColonyInterstitial ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            /** Request a new ad if ad is expiring */
            @Override
            public void onExpiring(AdColonyInterstitial ad) {
            }

            @Override
            public void onClicked(AdColonyInterstitial ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onClosed(AdColonyInterstitial ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onAudioStarted(AdColonyInterstitial ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoStart();
                }
            }

            @Override
            public void onAudioStopped(AdColonyInterstitial ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd();
                }
            }
        };

        AdColony.requestInterstitial(mZoneId, listener, adOptions);
    }

    @Override
    public boolean isAdReady() {
        return mAd != null && !mAd.isExpired();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return AdColonyATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkSDKVersion() {
        return AdColonyATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void show(Activity activity) {
        if (mAd != null && !mAd.isExpired()) {
            mAd.show();
        }
    }

    @Override
    public void destory() {
        try {
            AdColony.clearCustomMessageListeners();
            if (mAd != null) {
                try {
                    mAd.setListener(null);
                } catch (Throwable e) {
                }
                mAd.destroy();
                mAd = null;
            }
        } catch (Exception e) {
        }

    }

    @Override
    public String getNetworkName() {
        return AdColonyATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mZoneId;
    }


}
