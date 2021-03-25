/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.myoffer;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.anythink.basead.entity.OfferError;
import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.listeners.AdLoadListener;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.basead.myoffer.MyOfferSplashAd;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;

import java.util.Map;

public class MyOfferATSplashAdapter extends CustomSplashAdapter {
    String offer_id;

    MyOfferSplashAd mMyOfferSplashAd;

    BaseAdRequestInfo mMyOfferRequestInfo;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }

        if (serverExtras.containsKey(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY)) {
            mMyOfferRequestInfo = (BaseAdRequestInfo) serverExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY);
        }

        initSplashObject(context);

        mMyOfferSplashAd.load(new AdLoadListener() {
            @Override
            public void onAdDataLoaded() {

            }

            @Override
            public void onAdCacheLoaded() {
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
        return mMyOfferSplashAd != null && mMyOfferSplashAd.isReady();
    }

    @Override
    public void show(Activity activity, ViewGroup container) {
        if (mMyOfferSplashAd != null) {
            mMyOfferSplashAd.show(container);
        }
    }

    @Override
    public void destory() {
        if (mMyOfferSplashAd != null) {
            mMyOfferSplashAd.destory();
            mMyOfferSplashAd = null;
        }

        mMyOfferRequestInfo = null;
    }

    private void initSplashObject(Context context) {
        mMyOfferSplashAd = new MyOfferSplashAd(context, mMyOfferRequestInfo, offer_id, false);
        mMyOfferSplashAd.setListener(new AdEventListener() {

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

            }
        });
    }

    @Override
    public String getNetworkName() {
        return "MyOffer";
    }

    @Override
    public String getNetworkPlacementId() {
        return offer_id;
    }


    @Override
    public String getNetworkSDKVersion() {
        return Const.SDK_VERSION_NAME;
    }


}
