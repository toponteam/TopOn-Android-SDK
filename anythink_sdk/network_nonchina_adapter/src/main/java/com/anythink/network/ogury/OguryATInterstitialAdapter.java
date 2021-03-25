/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ogury;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;

import java.util.Map;

import io.presage.common.AdConfig;
import io.presage.interstitial.PresageInterstitial;
import io.presage.interstitial.PresageInterstitialCallback;

public class OguryATInterstitialAdapter extends CustomInterstitialAdapter {

    String mUnitId;

    private PresageInterstitial mPresageInterstitial;

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localsExtra) {

        String assetKey = "";
        String unitId = "";
        if (serverExtras.containsKey("key")) {
            assetKey = serverExtras.get("key").toString();
        }
        if (serverExtras.containsKey("unit_id")) {
            unitId = serverExtras.get("unit_id").toString();
        }

        if (TextUtils.isEmpty(assetKey) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "asset_key、unit_id could not be null.");
            }
            return;
        }
        mUnitId = unitId;


        OguryATInitManager.getInstance().initSDK(context, serverExtras, new OguryATInitManager.Callback() {
            @Override
            public void onSuccess() {
                init(context);
            }
        });
    }

    private void init(Context context) {
        AdConfig adConfig = new AdConfig(mUnitId);
        mPresageInterstitial = new PresageInterstitial(context.getApplicationContext(), adConfig);
        mPresageInterstitial.setInterstitialCallback(new PresageInterstitialCallback() {
            @Override
            public void onAdNotLoaded() {

            }

            @Override
            public void onAdLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdNotAvailable() {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "onAdNotAvailable");
                }
            }

            @Override
            public void onAdAvailable() {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onAdError(int code) {
                /*
                code 0: load failed
                code 1: phone not connected to internet.
                code 2: ad disabled
                code 3: various error (configuration file not synced)
                code 4: ad expires in 4 hours if it was not shown
                code 5: start method not called
                */
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("" + code, OguryATInitManager.getErrorMsg(code));
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onAdDisplayed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }
        });

        mPresageInterstitial.load();
    }

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            mPresageInterstitial.show();
        }
    }


    @Override
    public boolean isAdReady() {
        return mPresageInterstitial != null && mPresageInterstitial.isLoaded();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public String getNetworkSDKVersion() {
        return OguryATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void destory() {
        if (mPresageInterstitial != null) {
            mPresageInterstitial.setInterstitialCallback(null);
            mPresageInterstitial = null;
        }
    }

    @Override
    public String getNetworkName() {
        return OguryATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }
}
