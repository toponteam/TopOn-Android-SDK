/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.onlineapi;

import android.content.Context;
import android.view.View;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.basead.BaseAdUtils;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.innerad.OwnBannerAd;
import com.anythink.basead.innerad.OwnBaseAd;
import com.anythink.basead.innerad.OwnBaseAdConfig;
import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.listeners.AdLoadListener;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdRequestInfo;

import java.util.Map;

public class OnlineApiATBannerAdapter extends CustomBannerAdapter {

    private OwnBannerAd mBannerAd;
    BaseAdRequestInfo mRequestInfo;
    private View mBannerView;
    String mUnitId;

    Map<String, Object> mCustomMap;

    @Override
    public View getBannerView() {
        if (mBannerView == null) {
            if (mBannerAd != null && mBannerAd.isAdReady()) {
                mBannerView = mBannerAd.getBannerView();
            }
        }
        mCustomMap = BaseAdUtils.fillBaseAdCustomMap(mBannerAd);
        return mBannerView;
    }

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {

        initBannerAdObject(context, serverExtras);

        mBannerAd.load(new AdLoadListener() {
            @Override
            public void onAdDataLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onAdCacheLoaded() {
                mBannerView = mBannerAd.getBannerView();
                if (mLoadListener != null) {
                    if (mBannerView != null) {
                        mLoadListener.onAdCacheLoaded();
                    } else {
                        mLoadListener.onAdLoadError("", "Online bannerView = null");
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

    private void initBannerAdObject(Context context, Map<String, Object> serverExtra) {

        mUnitId = serverExtra.get("unit_id") != null ? serverExtra.get("unit_id").toString() : "";

        String bannerSize = "320x50";
        int showCloseButton = 0;

        if (serverExtra.containsKey("close_button")) {
            Object close_button = serverExtra.get("close_button");
            if (close_button != null) {
                showCloseButton = Integer.parseInt(close_button.toString());
            }
        }

        if (serverExtra.containsKey("size")) {
            Object size = serverExtra.get("size");
            if (size != null) {
                bannerSize = size.toString();
            }
        }


        mRequestInfo = (BaseAdRequestInfo) serverExtra.get(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY);
        mBannerAd = new OwnBannerAd(context, OwnBaseAd.OFFER_TYPE.ONLINE_API_OFFER_REQUEST_TYPE, mRequestInfo);
        mBannerAd.setAdConfig(new OwnBaseAdConfig.Builder()
                .showCloseButton(showCloseButton)
                .bannerSize(bannerSize)
                .build()
        );

        mBannerAd.setListener(new AdEventListener() {

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
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onDeeplinkCallback(isSuccess);
                }
            }
        });
    }

//    @Override
//    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
//        initBannerAdObject(context, serverExtras);
//        return true;
//    }


    @Override
    public void destory() {
        mBannerView = null;
        if (mBannerAd != null) {
            mBannerAd.setListener(null);
            mBannerAd.destroy();
            mBannerAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return "";
    }

    @Override
    public Map<String, Object> getNetworkInfoMap() {
        return mCustomMap;
    }
}
