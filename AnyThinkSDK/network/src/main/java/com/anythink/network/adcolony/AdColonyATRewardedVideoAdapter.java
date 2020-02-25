package com.anythink.network.adcolony;

import android.app.Activity;
import android.text.TextUtils;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyReward;
import com.adcolony.sdk.AdColonyRewardListener;
import com.adcolony.sdk.AdColonyZone;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;

import org.json.JSONArray;

import java.util.Map;

public class AdColonyATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    String mZoneId;
    String[] mZoneIds;

    AdColonyInterstitial mAd;

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
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

        mLoadResultListener = customRewardVideoListener;

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mZoneId)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid & mZoneId is empty."));
            }
            return;
        }

        /**Extra setting**/
        AdColonyRewardedVideoSetting setting = null;
        if (mediationSetting instanceof AdColonyRewardedVideoSetting) {
            setting = (AdColonyRewardedVideoSetting) mediationSetting;
        }



        /**
         * Configure AdColony in your launching Activity's onCreate() method so that cached ads can
         * be available as soon as possible.
         */
        AdColonyATInitManager.getInstance().initSDK(activity, serverExtras);

        AdColonyAppOptions app_options = AdColony.getAppOptions();
        if (setting != null) {
            /** Construct optional app options object to be sent with configure */
            app_options.setUserID(mUserId);
        }


        AdColonyAdOptions adOptions = new AdColonyAdOptions();
        if (setting != null) {
            /** Ad specific options to be sent with request */
            adOptions.enableConfirmationDialog(setting.isEnableConfirmationDialog())
                    .enableResultsDialog(setting.isEnableResultsDialog());
        } else {
            adOptions.enableConfirmationDialog(false)
                    .enableResultsDialog(false);
        }

        /** Create and set a reward listener */
        AdColony.setRewardListener(new AdColonyRewardListener() {
            @Override
            public void onReward(AdColonyReward reward) {
                /** Query reward object for info here */
                if (reward.success()) {
                    mImpressionListener.onReward(AdColonyATRewardedVideoAdapter.this);
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
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(AdColonyATRewardedVideoAdapter.this);
                }
            }

            /** Ad request was not filled */
            @Override
            public void onRequestNotFilled(AdColonyZone zone) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(AdColonyATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "No Fill!"));
                }
            }

            /** Ad opened, reset UI to reflect state change */
            @Override
            public void onOpened(AdColonyInterstitial ad) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(AdColonyATRewardedVideoAdapter.this);
                }
            }

            /** Request a new ad if ad is expiring */
            @Override
            public void onExpiring(AdColonyInterstitial ad) {

            }

            @Override
            public void onClicked(AdColonyInterstitial ad) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(AdColonyATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onClosed(AdColonyInterstitial ad) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(AdColonyATRewardedVideoAdapter.this);
                    mImpressionListener.onRewardedVideoAdClosed(AdColonyATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAudioStarted(AdColonyInterstitial ad) {

            }

            @Override
            public void onAudioStopped(AdColonyInterstitial ad) {

            }
        };

        AdColony.requestInterstitial(mZoneId, listener, adOptions);
    }

    @Override
    public boolean isAdReady() {
        return mAd != null && !mAd.isExpired();
    }

    @Override
    public void show(Activity activity) {
        if (mAd != null && !mAd.isExpired()) {
            mAd.show();
        }
    }

    @Override
    public void clean() {
        AdColony.clearCustomMessageListeners();

    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }


    @Override
    public String getSDKVersion() {
        return AdColonyATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return AdColonyATInitManager.getInstance().getNetworkName();
    }
}
