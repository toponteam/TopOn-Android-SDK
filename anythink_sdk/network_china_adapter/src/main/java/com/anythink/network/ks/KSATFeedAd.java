/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ks;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.kwad.sdk.api.KsFeedAd;

import java.util.List;

/**
 * Feed native express
 */
public class KSATFeedAd extends CustomNativeAd {

    Context context;
    KsFeedAd ksFeedAd;


    public KSATFeedAd(Context context, KsFeedAd ksFeedAd, boolean isVideoSoundEnable) {
        this.context = context;
        this.ksFeedAd = ksFeedAd;

        setAdData(isVideoSoundEnable);
    }

    private void setAdData(boolean isVideoSoundEnable) {
        ksFeedAd.setVideoSoundEnable(isVideoSoundEnable);
    }

    @Override
    public boolean isNativeExpress() {
        return true;
    }

    @Override
    public View getAdMediaView(Object... object) {
        try {
            return ksFeedAd.getFeedView(context);
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        ksFeedAd.setAdInteractionListener(new KsFeedAd.AdInteractionListener() {
            @Override
            public void onAdClicked() {
                notifyAdClicked();
            }

            @Override
            public void onAdShow() {

            }

            @Override
            public void onDislikeClicked() {
                notifyAdDislikeClick();
            }
        });
    }

    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {
        this.prepare(view, null, layoutParams);
    }

    @Override
    public void clear(View view) {
    }

    @Override
    public void destroy() {
        if (ksFeedAd != null) {
            ksFeedAd.setAdInteractionListener(null);
            ksFeedAd = null;
        }
        context = null;
    }
}
