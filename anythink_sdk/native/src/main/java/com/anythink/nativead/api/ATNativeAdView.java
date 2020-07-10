package com.anythink.nativead.api;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.nativead.unitgroup.BaseNativeAd;

/**
 * Created by Z on 2018/1/8.
 */

public class ATNativeAdView extends FrameLayout {
    private static final String TAG = ATNativeAdView.class.getSimpleName();
    ViewGroup mCustomAdView;

    BaseNativeAd mBaseNativeAd;

    boolean mHasSendImpression;

    boolean mIsInWindow;

    public ATNativeAdView(Context context) {
        super(context);
    }

    public ATNativeAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ATNativeAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    NativeAd mNativeAd;

    protected void renderView(NativeAd nativeAd, View developerView) {
        unregisterView(this);

        if (mCustomAdView != null) {
            mCustomAdView.removeAllViews();
        }
        removeAllViews();
        //Clear previous Ad before render current Ad
        try {
            if (mBaseNativeAd != null) {
                mBaseNativeAd.clear(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mNativeAd = nativeAd;
        mBaseNativeAd = nativeAd.mBaseNativeAd;

        mCustomAdView = mBaseNativeAd.getCustomAdContainer();

        if (mCustomAdView == null) {
            addView(developerView);
        } else {
            mCustomAdView.addView(developerView);
            addView(mCustomAdView);
        }

        mHasSendImpression = false;

        if (mIsInWindow && mNativeAd != null && getVisibility() == VISIBLE) {
            mNativeAd.recordImpression(this);
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
        if (mNativeAd != null && visibility == VISIBLE && getVisibility() == VISIBLE) {
            mNativeAd.recordImpression(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsInWindow = true;
        if (mNativeAd != null && getVisibility() == VISIBLE) {
            mNativeAd.recordImpression(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsInWindow = false;
    }


}
