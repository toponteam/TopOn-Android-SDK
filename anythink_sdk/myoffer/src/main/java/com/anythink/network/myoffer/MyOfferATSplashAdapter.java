/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.myoffer;

import android.content.Context;

import com.anythink.basead.entity.OfferError;
import com.anythink.basead.listeners.AdListener;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.MyOfferRequestInfo;
import com.anythink.basead.myoffer.MyOfferSplashAd;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;

import java.util.Map;

public class MyOfferATSplashAdapter extends CustomSplashAdapter {
    String offer_id;

    MyOfferSplashAd mMyOfferSplashAd;

    MyOfferRequestInfo mMyOfferRequestInfo;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }

        if (serverExtras.containsKey(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY)) {
            mMyOfferRequestInfo = (MyOfferRequestInfo) serverExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY);
        }

        initSplashObject(context);

        mMyOfferSplashAd.load();
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
        mMyOfferSplashAd = new MyOfferSplashAd(context, mMyOfferRequestInfo.placementId, offer_id, mMyOfferRequestInfo.myOfferSetting, getTrackingInfo().getmRequestId(), false);
        mMyOfferSplashAd.setListener(new AdListener() {
            @Override
            public void onAdDataLoaded() {

            }

            @Override
            public void onAdCacheLoaded() {
                if (mContainer != null) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                    mMyOfferSplashAd.show(mContainer);
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", "Splash Container has been released.");
                    }
                }
            }

            @Override
            public void onAdLoadFailed(OfferError error) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(error.getCode(), error.getDesc());
                }
            }

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
