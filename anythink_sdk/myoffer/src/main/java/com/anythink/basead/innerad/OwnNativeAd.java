/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad;

import android.content.Context;

import com.anythink.basead.entity.OfferError;
import com.anythink.basead.listeners.AdLoadListener;
import com.anythink.basead.listeners.AdNativeLoadListener;
import com.anythink.core.common.entity.BaseAdRequestInfo;


public class OwnNativeAd extends OwnBaseAd {

    public OwnNativeAd(Context context, OFFER_TYPE offerType, BaseAdRequestInfo ownBaseAdRequestInfo) {
        super(context, offerType, ownBaseAdRequestInfo);
    }

    public void load(final AdNativeLoadListener nativeLoadListener) {
        AdLoadListener innerListener = new AdLoadListener() {
            @Override
            public void onAdDataLoaded() {
            }

            @Override
            public void onAdCacheLoaded() {
                OwnUnifiedAd mOwnNativeAd = new OwnUnifiedAd(mContext, mBaseAdContent, mOwnBaseAdRequestInfo);
                if (nativeLoadListener != null) {
                    nativeLoadListener.onNativeAdLoaded(mOwnNativeAd);
                }
            }

            @Override
            public void onAdLoadFailed(OfferError error) {
                if (nativeLoadListener != null) {
                    nativeLoadListener.onNativeAdLoadError(error);
                }
            }
        };
        super.load(innerListener);
    }




}
