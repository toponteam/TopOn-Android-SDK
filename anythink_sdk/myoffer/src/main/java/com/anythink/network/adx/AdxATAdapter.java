/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.adx;

import android.content.Context;

import com.anythink.basead.innerad.OwnUnifiedAd;
import com.anythink.basead.innerad.OwnBaseAd;
import com.anythink.basead.innerad.OwnNativeAd;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.listeners.AdNativeLoadListener;
import com.anythink.core.api.BaseAd;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;

import java.util.Map;

public class AdxATAdapter extends CustomNativeAdapter {
    OwnNativeAd mAdxNativeAd;
    BaseAdRequestInfo mAdxRequestInfo;


    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        initNativeAdObject(context, serverExtras);

        final Context applicationContext = context.getApplicationContext();
        mAdxNativeAd.load(new AdNativeLoadListener() {
            @Override
            public void onNativeAdLoaded(OwnUnifiedAd... ownNativeAds) {
                AdxATNativeAd[] adxATNativeAds = new AdxATNativeAd[ownNativeAds.length];
                for (int i = 0; i < ownNativeAds.length; i++) {
                    adxATNativeAds[i] = new AdxATNativeAd(applicationContext, ownNativeAds[i]);
                }
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded(adxATNativeAds);
                }
            }

            @Override
            public void onNativeAdLoadError(OfferError offerError) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(offerError.getCode(), offerError.getDesc());
                }
            }
        });
    }

    private void initNativeAdObject(final Context context, Map<String, Object> serverExtra) {
        mAdxRequestInfo = (BaseAdRequestInfo) serverExtra.get(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY);
        mAdxNativeAd = new OwnNativeAd(context, OwnBaseAd.OFFER_TYPE.ADX_OFFER_REQUEST_TYPE, mAdxRequestInfo);
    }


//    @Override
//    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
//        initNativeAdObject(context, serverExtras);
//        return true;
//    }

//    @Override
//    public BaseAd getBaseAdObject(Context context) {
//        return null;
//    }

    @Override
    public void destory() {
        if (mAdxNativeAd != null) {
            mAdxNativeAd = null;
        }
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

}
