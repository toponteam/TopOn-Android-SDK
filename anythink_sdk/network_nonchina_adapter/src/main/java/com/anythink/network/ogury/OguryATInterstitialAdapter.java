package com.anythink.network.ogury;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;

import java.util.Map;

import io.presage.common.AdConfig;
import io.presage.interstitial.PresageInterstitial;
import io.presage.interstitial.PresageInterstitialCallback;

public class OguryATInterstitialAdapter extends CustomInterstitialAdapter {

    String mUnitId;

    private PresageInterstitial mPresageInterstitial;

    @Override
    public void loadInterstitialAd(final Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;

        String assetKey = "";
        String unitId = "";
        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service params is empty."));
            }
            return;
        } else {
            if (serverExtras.containsKey("key")) {
                assetKey = serverExtras.get("key").toString();
            }
            if (serverExtras.containsKey("unit_id")) {
                unitId = serverExtras.get("unit_id").toString();
            }

            if (TextUtils.isEmpty(assetKey) || TextUtils.isEmpty(unitId)) {
                if (mLoadResultListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "asset_key、unit_id could not be null.");
                    mLoadResultListener.onInterstitialAdLoadFail(this, adError);
                }
                return;
            }
        }
        mUnitId = unitId;


        OguryATInitManager.getInstance().initSDK(context, serverExtras, new OguryATInitManager.Callback() {
            @Override
            public void onSuccess() {
                init(((Activity) context));
            }
        });
    }

    private void init(Activity activity) {
        AdConfig adConfig = new AdConfig(mUnitId);
        mPresageInterstitial = new PresageInterstitial(activity.getApplicationContext(), adConfig);
        mPresageInterstitial.setInterstitialCallback(new PresageInterstitialCallback() {
            @Override
            public void onAdNotLoaded () {

            }

            @Override
            public void onAdLoaded () {
                if(mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoaded(OguryATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdNotAvailable () {
                if(mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(OguryATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "no ad available"));
                }
            }

            @Override
            public void onAdAvailable () {
                if(mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdDataLoaded(OguryATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdError (int code) {
                /*
                code 0: load failed
                code 1: phone not connected to internet.
                code 2: ad disabled
                code 3: various error (configuration file not synced)
                code 4: ad expires in 4 hours if it was not shown
                code 5: start method not called
                */
                if(mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(OguryATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + code, OguryATInitManager.getErrorMsg(code)));
                }
            }

            @Override
            public void onAdClosed () {
                if(mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose(OguryATInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdDisplayed () {
                if(mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow(OguryATInterstitialAdapter.this);
                }
            }
        });

        mPresageInterstitial.load();
    }

    @Override
    public void show(Context context) {
        if(isAdReady()) {
            mPresageInterstitial.show();
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public boolean isAdReady() {
        return mPresageInterstitial != null && mPresageInterstitial.isLoaded();
    }

    @Override
    public String getSDKVersion() {
        return OguryATConst.getSDKVersion();
    }

    @Override
    public void clean() {
        mPresageInterstitial = null;
    }

    @Override
    public String getNetworkName() {
        return OguryATInitManager.getInstance().getNetworkName();
    }
}
