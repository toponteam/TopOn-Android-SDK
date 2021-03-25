/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad;

import android.content.Context;
import android.view.ViewGroup;

import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.ui.SplashAdView;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdRequestInfo;

public class OwnSplashAd extends OwnBaseAd {

    AdEventListener mListener;

    public OwnSplashAd(Context context, OFFER_TYPE offerType, BaseAdRequestInfo ownBaseAdRequestInfo) {
        super(context, offerType, ownBaseAdRequestInfo);
    }


    /**
     * Only for Splash
     *
     * @param container
     */
    public void show(final ViewGroup container) {
        if (super.isAdReady()) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    container.addView(new SplashAdView(container.getContext(), mOwnBaseAdRequestInfo, mBaseAdContent, mListener));
                }
            });
        }

    }

    public void setListener(AdEventListener listener) {
        this.mListener = listener;
    }

    @Override
    public void destroy() {
        mListener = null;
    }

}
