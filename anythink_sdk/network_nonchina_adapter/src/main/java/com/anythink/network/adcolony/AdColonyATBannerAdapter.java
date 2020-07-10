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
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;

import org.json.JSONArray;

import java.util.Map;

public class AdColonyATBannerAdapter extends CustomBannerAdapter {
    private final String TAG = AdColonyATBannerAdapter.class.getSimpleName();
    String mZoneId;
    String[] mZoneIds;
    String mSize;

    AdColonyAdView adColontAdView;

    @Override
    public void loadBannerAd(ATBannerView bannerView, Context activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, final CustomBannerListener customBannerListener) {
//        serverExtras.put("app_id", "app251236acbb494d48a8");
//        serverExtras.put("zone_id", "vz627ee9b423cc4de09b");
//        serverExtras.put("zone_ids", "[\"vz627ee9b423cc4de09b\"]");
//        serverExtras.put("size", "320x50");
//        serverExtras.put("size", "300x250");
//        serverExtras.put("size", "728x90");
//        serverExtras.put("size", "160x600");


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
            if (customBannerListener != null) {
                customBannerListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity!"));
            }
            return;
        }

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mZoneId)) {
            if (customBannerListener != null) {
                customBannerListener.onBannerAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid & mZoneId is empty."));
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
                adColontAdView = ad;
                if (customBannerListener != null) {
                    customBannerListener.onBannerAdLoaded(AdColonyATBannerAdapter.this);
                }
            }

            public void onOpened(AdColonyAdView ad) {
            }

            public void onClosed(AdColonyAdView ad) {
                if (customBannerListener != null) {
                    customBannerListener.onBannerAdClose(AdColonyATBannerAdapter.this);
                }
            }

            public void onLeftApplication(AdColonyAdView ad) {
            }

            public void onClicked(AdColonyAdView ad) {
                if (customBannerListener != null) {
                    customBannerListener.onBannerAdClicked(AdColonyATBannerAdapter.this);
                }
            }

            public void onRequestNotFilled(AdColonyZone zone) {
                if (customBannerListener != null) {
                    customBannerListener.onBannerAdLoadFail(AdColonyATBannerAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "No Fill!"));
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
    public String getSDKVersion() {
        return AdColonyATConst.getNetworkVersion();
    }


    @Override
    public void clean() {
        AdColony.clearCustomMessageListeners();

    }

    @Override
    public String getNetworkName() {
        return AdColonyATInitManager.getInstance().getNetworkName();
    }


    @Override
    public View getBannerView() {
        return adColontAdView;
    }
}
