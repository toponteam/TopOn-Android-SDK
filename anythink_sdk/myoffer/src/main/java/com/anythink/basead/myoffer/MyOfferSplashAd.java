/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.content.Context;
import android.view.ViewGroup;

import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.myoffer.manager.MyOfferAdManager;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.basead.ui.SplashAdView;

import java.util.Map;

public class MyOfferSplashAd extends MyOfferBaseAd {

    AdEventListener mListener;

    public MyOfferSplashAd(Context context, BaseAdRequestInfo baseAdRequestInfo, String offerId, boolean isDefault) {
        super(context, baseAdRequestInfo, offerId, isDefault);
    }


    /**
     * Only for Splash
     *
     * @param container
     */
    public void show(final ViewGroup container) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                container.addView(new SplashAdView(container.getContext(), mRequestInfo, mMyOfferAd, mListener));
            }
        });

    }

    public void setListener(AdEventListener listener) {
        this.mListener = listener;
    }

    @Override
    public void show(Map<String, Object> extraMap) {

    }

    @Override
    public boolean isReady() {
        try {
            if (checkIsReadyParams()) {
                return MyOfferAdManager.getInstance(mContext).isReady(mMyOfferAd, mRequestInfo.baseAdSetting, mIsDefault);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public void destory() {
        mListener = null;
    }
}
