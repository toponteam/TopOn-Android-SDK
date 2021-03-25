/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.content.Context;
import android.view.View;

import com.anythink.basead.listeners.AdEventListener;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.basead.ui.BannerAdView;

import java.util.Map;

public class MyOfferBannerAd extends MyOfferBaseAd {
    private final String TAG = getClass().getSimpleName();

    AdEventListener mAdEventListener;

    public MyOfferBannerAd(Context context, BaseAdRequestInfo requestInfo, String offerId, boolean isDefault) {
        super(context, requestInfo, offerId, isDefault);
    }

    public void setListener(AdEventListener listener) {
        this.mAdEventListener = listener;
    }

    public View getBannerView() {
        if (this.isReady()) {
            return new BannerAdView(mContext, mRequestInfo, mMyOfferAd, mAdEventListener);
        }
        return null;
    }

    @Override
    public void show(Map<String, Object> extraMap) {

    }


    @Override
    public void destroy() {
        mAdEventListener = null;
    }
}
