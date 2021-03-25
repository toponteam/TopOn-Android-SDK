/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.onlineapi;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.basead.BaseAdUtils;
import com.anythink.basead.innerad.OwnUnifiedAd;
import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.ui.OwnNativeAdView;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;

import java.util.List;

public class OnlineApiATNativeAd extends CustomNativeAd {
    OwnUnifiedAd mNativeAd;
    Context mContext;

    public OnlineApiATNativeAd(Context context, OwnUnifiedAd adxNativeAd) {
        mContext = context.getApplicationContext();
        mNativeAd = adxNativeAd;
        mNativeAd.setListener(new AdEventListener() {
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
                notifyDeeplinkCallback(isSuccess);
            }
        });

        setNetworkInfoMap(BaseAdUtils.fillBaseAdCustomMap(mNativeAd.getBaseAdContent()));
        setAdChoiceIconUrl(mNativeAd.getAdChoiceIconUrl());
        setTitle(mNativeAd.getTitle());
        setDescriptionText(mNativeAd.getDesctiption());
        setIconImageUrl(mNativeAd.getIcon());
        setMainImageUrl(mNativeAd.getMainImageUrl());
        setCallToActionText(mNativeAd.getCallToAction());
    }

    @Override
    public ViewGroup getCustomAdContainer() {
        if (mNativeAd != null) {
            return new OwnNativeAdView(mContext);
        }
        return super.getCustomAdContainer();
    }

    @Override
    public View getAdMediaView(Object... object) {
        return mNativeAd.getMediaView(mContext);
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        if (mNativeAd != null) {
            mNativeAd.registerAdView(view, clickViewList);
        }
    }

    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {
        if (mNativeAd != null) {
            mNativeAd.registerAdView(view);
        }
    }

    @Override
    public void clear(View view) {
        if (mNativeAd != null) {
            mNativeAd.unregisterView();
        }
    }

    @Override
    public void destroy() {
        if (mNativeAd != null) {
            mNativeAd.setListener(null);
            mNativeAd.destroy();
        }
    }
}
