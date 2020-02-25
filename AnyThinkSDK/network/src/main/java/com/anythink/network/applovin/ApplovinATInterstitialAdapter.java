package com.anythink.network.applovin;

import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
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
                    mImpressListener.onInterstitialAdShow(ApplovinATInterstitialAdapter.this);
                }
            }

            @Override
            public void adHidden(AppLovinAd appLovinAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose(ApplovinATInterstitialAdapter.this);
                }
            }
        });
        mInterstitialAd.setAdClickListener(new AppLovinAdClickListener() {
            @Override
            public void adClicked(AppLovinAd appLovinAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked(ApplovinATInterstitialAdapter.this);
                }
            }
        });
        mInterstitialAd.setAdVideoPlaybackListener(new AppLovinAdVideoPlaybackListener() {
            @Override
            public void videoPlaybackBegan(AppLovinAd appLovinAd) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoStart(ApplovinATInterstitialAdapter.this);
                }
            }

            @Override
            public void videoPlaybackEnded(AppLovinAd appLovinAd, double v, boolean b) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd(ApplovinATInterstitialAdapter.this);
                }
            }
        });

        mApplovinSdk.getAdService().loadNextAdForZoneId(zoneid, new AppLovinAdLoadListener() {
            @Override
            public void adReceived(AppLovinAd appLovinAd) {
                mAppLovinAd = appLovinAd;
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(ApplovinATInterstitialAdapter.this);
                }
            }

            @Override
            public void failedToReceiveAd(int i) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(ApplovinATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, i + "", ""));
                }
            }
        });
    }


    @Override
    public void clean() {
        try {
            mAppLovinAd = null;
            if (mInterstitialAd != null) {
                mInterstitialAd.dismiss();
                mInterstitialAd = null;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }


    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service info  is empty."));
            }
            return;
        } else {
            if (serverExtras.containsKey("sdkkey") && serverExtras.containsKey("zone_id")) {
                sdkkey = (String) serverExtras.get("sdkkey");
                zoneid = (String) serverExtras.get("zone_id");
            } else {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "",  "sdkkey or zone_id is empty!"));
                }
                return;
            }
        }

        initAndLoad(context, serverExtras);
    }

    @Override
    public boolean isAdReady() {
        return mAppLovinAd != null;
    }

    @Override
    public void show(Context context) {
        if (mAppLovinAd != null) {
            mInterstitialAd.showAndRender(mAppLovinAd);
        }
    }

    @Override
    public String getSDKVersion() {
        return ApplovinATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return ApplovinATInitManager.getInstance().getNetworkName();
    }
}