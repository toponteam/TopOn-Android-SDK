/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.inmobi;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.ads.listeners.NativeAdEventListener;

import java.util.List;
import java.util.Map;

public class InmobiATNativeAd extends CustomNativeAd {

    private final String TAG = InmobiATNativeAd.class.getSimpleName();

    InMobiNative mInMobiNative;
    LoadCallbackListener mCustonNativeListener;

    public InmobiATNativeAd(Context context
            , LoadCallbackListener customNativeListener
            , String unitId
            , Map<String, Object> localExtras) {
        mInMobiNative = new InMobiNative(context, Long.parseLong(unitId), new NativeAdEventListener() {

            public void onAdLoadSucceeded(InMobiNative inMobiNative) {
                mInMobiNative = inMobiNative;
                setTitle(mInMobiNative.getAdTitle());
                setDescriptionText(mInMobiNative.getAdDescription());
                setIconImageUrl(mInMobiNative.getAdIconUrl());
                setCallToActionText(mInMobiNative.getAdCtaText());
                setMainImageUrl(mInMobiNative.getAdLandingPageUrl());
                setStarRating((double) mInMobiNative.getAdRating());

                mInMobiNative.setListener(new NativeAdEventListener() {
                    @Override
                    public void onAdImpressed(InMobiNative inMobiNative) {
                        notifyAdImpression();
                    }

                    @Override
                    public void onAdClicked(InMobiNative inMobiNative) {
                        //TODO Test
                        notifyAdClicked();
                    }
                });


                if (mCustonNativeListener != null) {
                    mCustonNativeListener.onSuccess(InmobiATNativeAd.this);
                }
                mCustonNativeListener = null;
            }

            public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                if (mCustonNativeListener != null) {
                    mCustonNativeListener.onFail(inMobiAdRequestStatus.getStatusCode() + "", inMobiAdRequestStatus.getMessage());
                }
                mCustonNativeListener = null;
            }

            public void onAdClicked(InMobiNative inMobiNative) {

            }

        });
        mCustonNativeListener = customNativeListener;

    }

    public void loadAd() {
        mInMobiNative.load();
    }

    // Lifecycle Handlers
    @Override
    public void prepare(final View view, FrameLayout.LayoutParams layoutParams) {
        registerView(view, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInMobiNative.reportAdClickAndOpenLandingPage();
            }
        });
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        for (View childView : clickViewList) {
            if (childView != null) {
                childView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mInMobiNative.reportAdClickAndOpenLandingPage();
                    }
                });
            }
        }
    }

    private void registerView(View view, View.OnClickListener clickListener) {
        if (view instanceof ViewGroup && view != mMediaView) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                registerView(child, clickListener);
            }
        } else {
            view.setOnClickListener(clickListener);
        }
    }

    private void unregisterView(View view) {
        if (view instanceof ViewGroup && view != mMediaView) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                registerView(child, null);
            }
        } else {
            view.setOnClickListener(null);
        }
    }

    @Override
    public void clear(final View view) {
        if (mMediaView != null && mMediaView instanceof ViewGroup) {
            mMediaView = null;
        }
        if (view != null) {
            unregisterView(view);
        }
    }

    View mMediaView;

    @Override
    public View getAdMediaView(Object... object) {
        try {
            if (mInMobiNative != null) {
                mMediaView = mInMobiNative.getPrimaryViewOfWidth((View) object[0], (ViewGroup) object[0], (int) object[1]);
                return mMediaView;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void destroy() {
        mMediaView = null;

        if (mInMobiNative != null) {
            mInMobiNative.destroy();
            mInMobiNative = null;
        }

    }

    boolean mIsAutoPlay;

    public void setIsAutoPlay(boolean isAutoPlay) {
        mIsAutoPlay = isAutoPlay;
    }

    interface LoadCallbackListener {
        void onSuccess(CustomNativeAd customNativeAd);

        void onFail(String errorCode, String errorMsg);
    }
}
