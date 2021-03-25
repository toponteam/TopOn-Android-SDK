/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.adx;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.anythink.basead.BaseAdUtils;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.innerad.OwnBaseAd;
import com.anythink.basead.innerad.OwnBaseAdConfig;
import com.anythink.basead.innerad.OwnSplashAd;
import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.listeners.AdLoadListener;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;

import java.util.Map;

public class AdxATSplashAdapter extends CustomSplashAdapter {

    OwnSplashAd mAdxSplashAd;
    BaseAdRequestInfo mAdxRequestInfo;
    Map<String, Object> mCustomMap;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {

        initSplashAdObject(context, serverExtras);

        mAdxSplashAd.load(new AdLoadListener() {
            @Override
            public void onAdDataLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onAdCacheLoaded() {
                mCustomMap = BaseAdUtils.fillBaseAdCustomMap(mAdxSplashAd);
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdLoadFailed(OfferError error) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(error.getCode(), error.getDesc());
                }
            }
        });
    }

    @Override
    public boolean isAdReady() {
        mCustomMap = BaseAdUtils.fillBaseAdCustomMap(mAdxSplashAd);
        return mAdxSplashAd != null && mAdxSplashAd.isAdReady();
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        if (mAdxSplashAd != null) {
            mAdxSplashAd.show(container);
        }
    }

    @Override
    public void destory() {
        if (mAdxSplashAd != null) {
            mAdxSplashAd.destroy();
            mAdxSplashAd = null;
        }

        mAdxRequestInfo = null;
    }

    private void initSplashAdObject(Context context, Map<String, Object> serverExtra) {

        int orientation = 1;
        int countdown = 5;
        int allows_skip = 1;

        if (serverExtra.containsKey("orientation")) {
            Object orientationObj = serverExtra.get("orientation");
            if (orientationObj != null) {
                orientation = Integer.parseInt(orientationObj.toString());
            }
        }

        if (serverExtra.containsKey("countdown")) {
            Object countdownObj = serverExtra.get("countdown");
            if (countdownObj != null) {
                countdown = Integer.parseInt(countdownObj.toString()) * 1000;
            }
        }

        if (serverExtra.containsKey("allows_skip")) {
            Object allowsSkipObj = serverExtra.get("allows_skip");
            if (allowsSkipObj != null) {
                allows_skip = Integer.parseInt(allowsSkipObj.toString());
                // convert
                if (allows_skip == 0) {
                    allows_skip = 1;
                } else if (allows_skip == 1) {
                    allows_skip = 0;
                }
            }
        }

        mAdxRequestInfo = (BaseAdRequestInfo) serverExtra.get(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY);
        mAdxSplashAd = new OwnSplashAd(context, OwnBaseAd.OFFER_TYPE.ADX_OFFER_REQUEST_TYPE, mAdxRequestInfo);
        mAdxSplashAd.setAdConfig(new OwnBaseAdConfig.Builder()
                .orientation(orientation)
                .countdownTime(countdown)
                .canSkip(allows_skip)
                .build()
        );

        mAdxSplashAd.setListener(new AdEventListener() {

            @Override
            public void onAdShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdShow();
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdDismiss();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onSplashAdClicked();
                }
            }

            @Override
            public void onDeeplinkCallback(boolean isSuccess) {
                if (mImpressionListener != null) {
                    mImpressionListener.onDeeplinkCallback(isSuccess);
                }
            }
        });
    }

    @Override
    public String getNetworkName() {
        return "Adx";
    }

    @Override
    public String getNetworkPlacementId() {
        return mAdxRequestInfo.placementId;
    }


    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public Map<String, Object> getNetworkInfoMap() {
        return mCustomMap;
    }

}