package com.anythink.network.applovin;

import android.app.Activity;
import android.content.Context;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinSdk;

import java.util.Map;

/**
 * Created by Z on 2018/6/27.
 */

public class ApplovinATInterstitialAdapter extends CustomInterstitialAdapter {
    private static final String TAG = ApplovinATInterstitialAdapter.class.getSimpleName();

    String sdkkey = "", zoneid = "";
    AppLovinAd mAppLovinAd;
    AppLovinInterstitialAdDialog mInterstitialAd;

    /***
     * init and load
     */
    private void initAndLoad(Context context, Map<String, Object> serverExtras) {
        AppLovinSdk mApplovinSdk = ApplovinATInitManager.getInstance().initSDK(context, sdkkey, serverExtras);

        mInterstitialAd = AppLovinInterstitialAd.create(mApplovinSdk, context.getApplicationContext());
        mInterstitialAd.setAdDisplayListener(new AppLovinAdDisplayListener() {
            @Override
            public void adDisplayed(AppLovinAd appLovinAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }
        });
        mInterstitialAd.setAdClickListener(new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }
        });
        mInterstitialAd.setAdVideoPlaybackListener(new AppLovinAdVideoPlaybackListener() {
            @Override
            public void videoPlaybackBegan(AppLovinAd appLovinAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoStart();
                }
            }

            @Override
            public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd();
                }
            }
        });

        mApplovinSdk.getAdService().loadNextAdForZoneId(zoneid, new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd appLovinAd) {
                mAppLovinAd = appLovinAd;
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
        });
    }


    @Override
    public void destory() {
        try {
            mAppLovinAd = null;
            if (mInterstitialAd != null) {
                mInterstitialAd.setAdClickListener(null);
                mInterstitialAd.setAdDisplayListener(null);
                mInterstitialAd.setAdVideoPlaybackListener(null);
                mInterstitialAd = null;
            }
        } catch (Exception e) {

        }
    }


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {
        if (serverExtras.containsKey("sdkkey") && serverExtras.containsKey("zone_id")) {
            sdkkey = (String) serverExtras.get("sdkkey");
            zoneid = (String) serverExtras.get("zone_id");
        } else {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "sdkkey or zone_id is empty!");
            }
            return;
        }

        initAndLoad(context, serverExtras);
    }

    @Override
    public boolean isAdReady() {
        return mAppLovinAd != null;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return ApplovinATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        if (mAppLovinAd != null) {
            mInterstitialAd.showAndRender(mAppLovinAd);
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return ApplovinATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return ApplovinATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return zoneid;
    }
}