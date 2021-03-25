/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.adx;

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

public class AdxATNativeAd extends CustomNativeAd {
    OwnUnifiedAd mAdxNativeAd;
    Context mContext;

    public AdxATNativeAd(Context context, OwnUnifiedAd adxNativeAd) {
        mContext = context.getApplicationContext();
        mAdxNativeAd = adxNativeAd;
        mAdxNativeAd.setListener(new AdEventListener() {
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

        setNetworkInfoMap(BaseAdUtils.fillBaseAdCustomMap(mAdxNativeAd.getBaseAdContent()));
        setAdChoiceIconUrl(mAdxNativeAd.getAdChoiceIconUrl());
        setTitle(mAdxNativeAd.getTitle());
        setDescriptionText(mAdxNativeAd.getDesctiption());
        setIconImageUrl(mAdxNativeAd.getIcon());
        setMainImageUrl(mAdxNativeAd.getMainImageUrl());
        setCallToActionText(mAdxNativeAd.getCallToAction());
    }

    @Override
    public View getAdMediaView(Object... object) {
        return mAdxNativeAd.getMediaView(mContext);
    }

    @Override
    public ViewGroup getCustomAdContainer() {
        if (mAdxNativeAd != null) {
            return new OwnNativeAdView(mContext);
        }
        return super.getCustomAdContainer();
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        if (mAdxNativeAd != null) {
            mAdxNativeAd.registerAdView(view, clickViewList);
        }
    }

    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {
        if (mAdxNativeAd != null) {
            mAdxNativeAd.registerAdView(view);
        }
    }

    @Override
    public void clear(View view) {
        if (mAdxNativeAd != null) {
            mAdxNativeAd.unregisterView();
        }
    }

    @Override
    public void destroy() {
        if (mAdxNativeAd != null) {
            mAdxNativeAd.setListener(null);
            mAdxNativeAd.destroy();
        }
    }
}
