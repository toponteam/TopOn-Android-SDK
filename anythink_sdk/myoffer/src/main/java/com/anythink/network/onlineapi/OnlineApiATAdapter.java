/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.onlineapi;

import android.content.Context;

import com.anythink.basead.entity.OfferError;
import com.anythink.basead.innerad.OwnBaseAd;
import com.anythink.basead.innerad.OwnNativeAd;
import com.anythink.basead.innerad.OwnUnifiedAd;
import com.anythink.basead.listeners.AdNativeLoadListener;
import com.anythink.core.api.BaseAd;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.network.adx.AdxATNativeAd;

import java.util.Map;

public class OnlineApiATAdapter extends CustomNativeAdapter {

    OwnNativeAd mNativeAd;
    BaseAdRequestInfo mRequestInfo;

    String mUnitId;

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
        initNativeAdObject(context, serverExtras);

        final Context applicationContext = context.getApplicationContext();
        mNativeAd.load(new AdNativeLoadListener() {
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
        mUnitId = serverExtra.get("unit_id") != null ? serverExtra.get("unit_id").toString() : "";
        mRequestInfo = (BaseAdRequestInfo) serverExtra.get(Const.NETWORK_REQUEST_PARAMS_KEY.BASE_AD_PARAMS_KEY);
        mNativeAd = new OwnNativeAd(context, OwnBaseAd.OFFER_TYPE.ONLINE_API_OFFER_REQUEST_TYPE, mRequestInfo);
    }


//    @Override
//    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtra) {
//        initNativeAdObject(context, serverExtras);
//        return true;
//    }


//    @Override
//    public BaseAd getBaseAdObject(Context context) {
//        if (mNativeAd != null && mNativeAd.isReady()) {
//            OnlineApiATNativeAd adxATNativeAd = new OnlineApiATNativeAd(context, mNativeAd);
//            return adxATNativeAd;
//        }
//        return null;
//    }

    @Override
    public void destory() {
        if (mNativeAd != null) {
            mNativeAd = null;
        }
    }

    @Override
    public String getNetworkName() {
        return "";
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }


    @Override
    public String getNetworkSDKVersion() {
        return "";
    }

}
