/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.api;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Z on 2018/1/8.
 */

public class ATNativeAdView extends FrameLayout {
    private static final String TAG = ATNativeAdView.class.getSimpleName();

    boolean mIsInWindow;
    int mNativeAdId;

    public ATNativeAdView(Context context) {
        super(context);
    }

    public ATNativeAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ATNativeAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    View mAdView;
    NativeAd.ImpressionEventListener mImpressionEventListener;

    protected void renderView(int nativeAdId, View adView, NativeAd.ImpressionEventListener impressionEventListener) {
        if (mAdView != null) {
            unregisterView((ViewGroup) mAdView);
            removeView(mAdView);
        }

        mAdView = adView;
        mNativeAdId = nativeAdId;
        mImpressionEventListener = impressionEventListener;

        addView(mAdView);
        if (mIsInWindow && getVisibility() == VISIBLE) {
            callbackImpression();
        }
    }


    public void clearImpressionListener(int hashCode) {
        if (mNativeAdId == hashCode) {
            mImpressionEventListener = null;
        }
    }

    private void unregisterView(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            child.setOnClickListener(null);
            if (child instanceof ViewGroup) {
                unregisterView((ViewGroup) child);
            } else {
                child.setOnClickListener(null);
            }
        }
    }


    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE && getVisibility() == VISIBLE) {
            callbackImpression();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsInWindow = true;
        if (getVisibility() == VISIBLE) {
            callbackImpression();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsInWindow = false;
    }

    private void callbackImpression() {
        if (mImpressionEventListener != null) {
            mImpressionEventListener.onImpression();
        }
    }

    public void destory() {
        mImpressionEventListener = null;
    }
}
