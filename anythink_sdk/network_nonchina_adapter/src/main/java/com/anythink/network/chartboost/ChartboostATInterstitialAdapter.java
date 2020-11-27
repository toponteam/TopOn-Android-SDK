package com.anythink.network.chartboost;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.Model.CBError;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */


public class ChartboostATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = ChartboostATInterstitialAdapter.class.getSimpleName();

    String location = CBLocation.LOCATION_DEFAULT;

    boolean isRewared = false;

    /***
     * init and load
     */
    private void initAndLoad(Context context, Map<String, Object> serverExtras) {
        ChartboostATInitManager.getInstance().initSDK(context, serverExtras, new ChartboostATInitManager.InitCallback() {
            @Override
            public void didInitialize() {
                ChartboostATInterstitialAdapter.this.didInitialize();
            }
        });
//        Chartboost.setShouldPrefetchVideoContent(true);
    }

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String appid = (String) serverExtras.get("app_id");
        String appkey = (String) serverExtras.get("app_signature");
        location = (String) serverExtras.get("location");

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(location)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " app_id ,app_signature or location is empty.");
            }
            return;
        }

        initAndLoad(context, serverExtras);
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        if (serverExtras != null) {
            if (serverExtras.containsKey("app_id") && serverExtras.containsKey("app_signature") && serverExtras.containsKey("location")) {
                location = (String) serverExtras.get("location");
                ChartboostATInitManager.getInstance().putAdapter(location, this);
                return true;
            }
        }
        return false;
    }

    /***
     * load ad
     */
    public void startload() {
        ChartboostATInitManager.getInstance().loadInterstitial(location, this);
    }

    @Override
    public void destory() {
    }

    @Override
    public String getNetworkName() {
        return ChartboostATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return location;
    }


    @Override
    public boolean isAdReady() {
        return Chartboost.hasInterstitial(location);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return ChartboostATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        ChartboostATInitManager.getInstance().putAdapter(location, this);
        Chartboost.showInterstitial(location);
    }


    @Override
    public String getNetworkSDKVersion() {
        return ChartboostATConst.getNetworkVersion();
    }


    /**
     * -------------------------------------------callback-------------------------------------------------------
     **/

    public boolean shouldRequestInterstitial(String location) {
        return true;
    }

    public boolean shouldDisplayInterstitial(String location) {
        return true;
    }

    public void didCacheInterstitial(String location) {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }
    }

    public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError("" + error.name(), " " + error.toString());
        }
    }

    public void willDisplayInterstitial(String location) {
    }

    public void didDismissInterstitial(String location) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose();
        }
    }

    public void didCloseInterstitial(String location) {

    }

    public void didClickInterstitial(String location) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked();
        }
    }

    public void didCompleteInterstitial(String location) {
        isRewared = true;
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoEnd();
        }
    }

    public void didDisplayInterstitial(String location) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow();
            mImpressListener.onInterstitialAdVideoStart();
        }
    }

    public void didInitialize() {
        try {
            startload();
        } catch (Throwable e) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", e.getMessage());
            }
        }
    }
}