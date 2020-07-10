package com.anythink.network.adcolony;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;

import org.json.JSONArray;

import java.util.Map;

public class AdColonyATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = AdColonyATInterstitialAdapter.class.getSimpleName();
    String mZoneId;
    String[] mZoneIds;

    AdColonyInterstitial mAd;

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
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

        mLoadResultListener = customInterstitialListener;

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity!"));
            }
            return;
        }

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mZoneId)) {
            if (mLoadResultListener != null) {
                log(TAG, "appid and zonid is empty!");
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid & mZoneId is empty."));
            }
            return;
        }

        /**
         * Configure AdColony in your launching Activity's onCreate() method so that cached ads can
         * be available as soon as possible.
         */
        AdColonyATInitManager.getInstance().initSDK(context, serverExtras);

        AdColonyAdOptions adOptions = new AdColonyAdOptions();
        adOptions.enableConfirmationDialog(false)
                .enableResultsDialog(false);


        /**
         * Set up listener for interstitial ad callbacks. You only need to implement the callbacks
         * that you care about. The only required callback is onRequestFilled, as this is the only
         * way to get an ad object.
         */
        AdColonyInterstitialListener listener = new AdColonyInterstitialListener() {
            /** Ad passed back in request filled callback, ad can now be shown */
            @Override
            public void onRequestFilled(AdColonyInterstitial ad) {
                mAd = ad;
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(AdColonyATInterstitialAdapter.this);
                }
            }

            /** Ad request was not filled */
            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(AdColonyATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "No Fill!"));
                }
            }

            /** Ad opened, reset UI to reflect state change */
            @Override
            public void onOpened(AdColonyInterstitial ad) {
                log(TAG, "onOpened");
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow(AdColonyATInterstitialAdapter.this);
                }
            }

            /** Request a new ad if ad is expiring */
            @Override
            public void onExpiring(AdColonyInterstitial ad) {
                log(TAG, "onExpiring");
            }

            @Override
            public void onClicked(AdColonyInterstitial ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked(AdColonyATInterstitialAdapter.this);
                }
            }

            @Override
            public void onClosed(AdColonyInterstitial ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose(AdColonyATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAudioStarted(AdColonyInterstitial ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoStart(AdColonyATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAudioStopped(AdColonyInterstitial ad) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd(AdColonyATInterstitialAdapter.this);
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
    public String getSDKVersion() {
        return AdColonyATConst.getNetworkVersion();
    }

    @Override
    public void show(Context context) {
        if (mAd != null && !mAd.isExpired()) {
            mAd.show();
        }
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
    public void onResume() {

    }

    @Override
    public void onPause() {

    }


}
