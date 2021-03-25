/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.adx;

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

public class AdxATBannerAdapter extends CustomBannerAdapter {

    private OwnBannerAd mAdxBannerAd;
    BaseAdRequestInfo mAdxRequestInfo;
    private View mBannerView;

    Map<String, Object> mCustomMap;

    @Override
    public View getBannerView() {
        if (mBannerView == null) {
            if (mAdxBannerAd != null && mAdxBannerAd.isAdReady()) {
                mBannerView = mAdxBannerAd.getBannerView();
            }
        }
        mCustomMap = BaseAdUtils.fillBaseAdCustomMap(mAdxBannerAd);

        return mBannerView;
    }


    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {

        initBannerAdObject(context, serverExtras);

        mAdxBannerAd.load(new AdLoadListener() {
            @Override
            public void onAdDataLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onAdCacheLoaded() {
                mBannerView = mAdxBannerAd.getBannerView();
                if (mLoadListener != null) {
                    if (mBannerView != null) {
                        mCustomMap = BaseAdUtils.fillBaseAdCustomMap(mAdxBannerAd);
                        mLoadListener.onAdCacheLoaded();
                    } else {
                        mLoadListener.onAdLoadError("", "Adx bannerView = null");
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


        mAdxRequestInfo = (BaseAdRequestInfo) serverExtra.get(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY);
        mAdxBannerAd = new OwnBannerAd(context, OwnBaseAd.OFFER_TYPE.ADX_OFFER_REQUEST_TYPE, mAdxRequestInfo);
        mAdxBannerAd.setAdConfig(new OwnBaseAdConfig.Builder()
                .showCloseButton(showCloseButton)
                .bannerSize(bannerSize)
                .build()
        );

        mAdxBannerAd.setListener(new AdEventListener() {

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
        if (mAdxBannerAd != null) {
            mAdxBannerAd.setListener(null);
            mAdxBannerAd.destroy();
            mAdxBannerAd = null;
        }
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
    public String getNetworkName() {
        return "Adx";
    }

    @Override
    public Map<String, Object> getNetworkInfoMap() {
        return mCustomMap;
    }
}
