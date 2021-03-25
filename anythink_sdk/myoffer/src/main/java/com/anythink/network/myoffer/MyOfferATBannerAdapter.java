/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.myoffer;

import android.content.Context;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.listeners.AdLoadListener;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.basead.myoffer.MyOfferBannerAd;

import java.util.Map;

public class MyOfferATBannerAdapter extends CustomBannerAdapter {

    String offer_id;
    private MyOfferBannerAd mMyOfferBannerAd;
    private View mBannerView;

    private boolean isDefaultOffer = false; //用于判断兜底offer的
    BaseAdRequestInfo mMyOfferRequestInfo;

    @Override
    public View getBannerView() {
        if (mBannerView == null) {
            if (mMyOfferBannerAd != null && mMyOfferBannerAd.isReady()) {
                mBannerView = mMyOfferBannerAd.getBannerView();
            }
        }
        return mBannerView;
    }

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY)) {
            mMyOfferRequestInfo = (BaseAdRequestInfo) serverExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY);
        }

        initBannerAdObject(context);

        mMyOfferBannerAd.load(new AdLoadListener() {
            @Override
            public void onAdDataLoaded() {

            }

            @Override
            public void onAdCacheLoaded() {
                mBannerView = mMyOfferBannerAd.getBannerView();

                if (mLoadListener != null) {
                    if (mBannerView != null) {
                        mLoadListener.onAdCacheLoaded();
                    } else {
                        mLoadListener.onAdLoadError("", "MyOffer bannerView = null");
                    }
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

    private void initBannerAdObject(Context context) {
        mMyOfferBannerAd = new MyOfferBannerAd(context, mMyOfferRequestInfo, offer_id, isDefaultOffer);
        mMyOfferBannerAd.setListener(new AdEventListener() {

            @Override
            public void onAdShow() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }

            @Override
            public void onDeeplinkCallback(boolean isSuccess) {

            }
        });
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }

        if (serverExtras.containsKey(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY)) {
            mMyOfferRequestInfo = (BaseAdRequestInfo) serverExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY);
        }

        if (serverExtras.containsKey(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG)) {
            isDefaultOffer = (Boolean) serverExtras.get(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG);
        }

        initBannerAdObject(context);
        return true;
    }

    @Override
    public void destory() {
        mBannerView = null;
        if (mMyOfferBannerAd != null) {
            mMyOfferBannerAd.setListener(null);
            mMyOfferBannerAd.destroy();
            mMyOfferBannerAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return offer_id;
    }

    @Override
    public String getNetworkSDKVersion() {
        return Const.SDK_VERSION_NAME;
    }

    @Override
    public String getNetworkName() {
        return "MyOffer";
    }
}
