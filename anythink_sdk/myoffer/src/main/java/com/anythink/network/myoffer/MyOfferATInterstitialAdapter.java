/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.myoffer;

import android.app.Activity;
import android.content.Context;

import com.anythink.basead.entity.OfferError;
import com.anythink.basead.listeners.VideoAdListener;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.MyOfferRequestInfo;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.basead.myoffer.MyOfferBaseAd;
import com.anythink.basead.myoffer.MyOfferInterstitialAd;

import java.util.HashMap;
import java.util.Map;

public class MyOfferATInterstitialAdapter extends CustomInterstitialAdapter {

    private String offer_id = "";
    private MyOfferInterstitialAd mMyOfferInterstitialAd;
    private boolean isDefaultOffer = false; //用于判断兜底offer的

    MyOfferRequestInfo mMyOfferRequestInfo;

    /**
     * @param context
     * @param serverExtras key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     */
    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localMap) {

        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY)) {
            mMyOfferRequestInfo = (MyOfferRequestInfo) serverExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY);
        }

        initInterstitialAdObject(context);

        mMyOfferInterstitialAd.load();
    }

    private void initInterstitialAdObject(Context context) {
        mMyOfferInterstitialAd = new MyOfferInterstitialAd(context, mMyOfferRequestInfo.placementId, offer_id, mMyOfferRequestInfo.myOfferSetting, isDefaultOffer);
        mMyOfferInterstitialAd.setListener(new VideoAdListener() {
            @Override
            public void onVideoAdPlayStart() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoStart();
                }
            }

            @Override
            public void onVideoAdPlayEnd() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoEnd();
                }
            }

            @Override
            public void onVideoShowFailed(OfferError error) {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdVideoError(error.getCode(), error.getDesc());
                }
            }

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

            @Override
            public void onAdShow() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClose();
                }
            }

            @Override
            public void onAdClick() {
                if (mImpressListener != null) {
                    mImpressListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onRewarded() {

            }
        });
    }

    /**
     * @param context
     * @param serverExtras key: myoffer_setting(Play Setting)，topon_placement(PlacementId)，my_oid(MyOfferId)
     * @param localMap
     * @return
     */
    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localMap) {
        if (serverExtras.containsKey("my_oid")) {
            offer_id = serverExtras.get("my_oid").toString();
        }
        if (serverExtras.containsKey(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY)) {
            mMyOfferRequestInfo = (MyOfferRequestInfo) serverExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.MYOFFER_PARAMS_KEY);
        }
        if (serverExtras.containsKey(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG)) {
            isDefaultOffer = (Boolean) serverExtras.get(MyOfferAPIProxy.MYOFFER_DEFAULT_TAG);
        }


        initInterstitialAdObject(context);
        return true;
    }

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            Map<String, Object> extraMap = new HashMap<>(1);

            int orientation = CommonDeviceUtil.orientation(activity);
            extraMap.put(MyOfferBaseAd.EXTRA_REQUEST_ID, mMyOfferRequestInfo.requestId);
            extraMap.put(MyOfferBaseAd.EXTRA_SCENARIO, mScenario);
            extraMap.put(MyOfferBaseAd.EXTRA_ORIENTATION, orientation);
            mMyOfferInterstitialAd.show(extraMap);
        }
    }


    @Override
    public boolean isAdReady() {
        if (mMyOfferInterstitialAd != null) {
            return mMyOfferInterstitialAd.isReady();
        }
        return false;
    }


    @Override
    public String getNetworkSDKVersion() {
        return Const.SDK_VERSION_NAME;
    }

    @Override
    public void destory() {
        if (mMyOfferInterstitialAd != null) {
            mMyOfferInterstitialAd.setListener(null);
            mMyOfferInterstitialAd = null;
        }
    }

    @Override
    public String getNetworkName() {
        return "MyOffer";
    }

    @Override
    public String getNetworkPlacementId() {
        return offer_id;
    }
}
