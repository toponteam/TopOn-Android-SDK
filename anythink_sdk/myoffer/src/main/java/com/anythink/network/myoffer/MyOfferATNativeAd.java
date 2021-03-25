/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.myoffer;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.myoffer.MyOfferNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;

import java.util.List;

public class MyOfferATNativeAd extends CustomNativeAd {
    MyOfferNativeAd mMyOfferNativeAd;
    Context mContext;

    public MyOfferATNativeAd(Context context, MyOfferNativeAd myOfferNativeAd) {
        mContext = context.getApplicationContext();
        mMyOfferNativeAd = myOfferNativeAd;
        mMyOfferNativeAd.setListener(new AdEventListener() {

            @Override
            public void onAdShow() {
                notifyAdImpression();
            }

            @Override
            public void onAdClosed() {

            }

            @Override
            public void onAdClick() {
                notifyAdClicked();
            }

            @Override
            public void onDeeplinkCallback(boolean isSuccess) {

            }
        });

        setAdChoiceIconUrl(mMyOfferNativeAd.getAdChoiceIconUrl());
        setTitle(mMyOfferNativeAd.getTitle());
        setDescriptionText(mMyOfferNativeAd.getDesctiption());
        setIconImageUrl(mMyOfferNativeAd.getIcon());
        setMainImageUrl(mMyOfferNativeAd.getMainImageUrl());
        setCallToActionText(mMyOfferNativeAd.getCallToAction());
    }

    @Override
    public View getAdMediaView(Object... object) {
        return mMyOfferNativeAd.getMediaView();
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        if (mMyOfferNativeAd != null) {
            mMyOfferNativeAd.registerAdView(view, clickViewList);
        }
    }

    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {
        if (mMyOfferNativeAd != null) {
            mMyOfferNativeAd.registerAdView(view);
        }
    }

    @Override
    public void clear(View view) {
        if (mMyOfferNativeAd != null) {
            mMyOfferNativeAd.unregisterView();
        }
    }

    @Override
    public void destroy() {
        if (mMyOfferNativeAd != null) {
            mMyOfferNativeAd.setListener(null);
            mMyOfferNativeAd.destory();
        }
    }
}
