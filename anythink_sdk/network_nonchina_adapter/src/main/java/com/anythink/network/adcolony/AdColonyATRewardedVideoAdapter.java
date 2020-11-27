package com.anythink.network.adcolony;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyReward;
import com.adcolony.sdk.AdColonyRewardListener;
import com.adcolony.sdk.AdColonyZone;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import org.json.JSONArray;

import java.util.Map;

public class AdColonyATRewardedVideoAdapter extends CustomRewardVideoAdapter {
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

        AdColonyAppOptions appOptions = AdColony.getAppOptions();
        /** Construct optional app options object to be sent with configure */
        appOptions.setUserID(mUserId);

        /** Create and set a reward listener */
        AdColony.setRewardListener(new AdColonyRewardListener() {
            @Override
            public void onReward(AdColonyReward reward) {
                /** Query reward object for info here */
                if (reward.success()) {
                    if (mImpressionListener != null) {
                        mImpressionListener.onReward();
                    }
                }
            }
        });

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
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            /** Ad request was not filled */
            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "onRequestNotFilled!");
                }
            }

            /** Ad opened, reset UI to reflect state change */
            @Override
            public void onOpened(AdColonyInterstitial ad) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            /** Request a new ad if ad is expiring */
            @Override
            public void onExpiring(AdColonyInterstitial ad) {

            }

            @Override
            public void onClicked(AdColonyInterstitial ad) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }

            @Override
            public void onClosed(AdColonyInterstitial ad) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAudioStarted(AdColonyInterstitial ad) {

            }

            @Override
            public void onAudioStopped(AdColonyInterstitial ad) {

            }
        };

        AdColony.requestInterstitial(mZoneId, listener, new AdColonyAdOptions());
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
    public String getNetworkSDKVersion() {
        return AdColonyATConst.getNetworkVersion();
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
