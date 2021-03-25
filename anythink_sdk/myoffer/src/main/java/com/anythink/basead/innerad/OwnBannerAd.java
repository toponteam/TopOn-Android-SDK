/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad;

import android.content.Context;
import android.view.View;

import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.ui.BannerAdView;
import com.anythink.core.common.entity.BaseAdRequestInfo;


public class OwnBannerAd extends OwnBaseAd {
    private final String TAG = getClass().getSimpleName();

    AdEventListener mListener;

    public OwnBannerAd(Context context, OFFER_TYPE offerType, BaseAdRequestInfo ownBaseAdRequestInfo) {
        super(context, offerType, ownBaseAdRequestInfo);
    }

    public void setListener(AdEventListener listener) {
        this.mListener = listener;
    }

    public View getBannerView() {
        if (super.isAdReady()) {

            return new BannerAdView(mContext, mOwnBaseAdRequestInfo, mBaseAdContent, mListener);
        }
        return null;
    }


    @Override
    public void destroy() {
        super.destroy();
        mListener = null;
    }

}
