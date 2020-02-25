package com.anythink.network.inmobi;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiNative;
import com.inmobi.ads.listeners.NativeAdEventListener;

import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/12.
 */
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

                mCustonNativeListener.onSuccess(InmobiATNativeAd.this);
            }

            public void onAdLoadFailed(InMobiNative inMobiNative, InMobiAdRequestStatus inMobiAdRequestStatus) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, inMobiAdRequestStatus.getStatusCode() + "", inMobiAdRequestStatus.getMessage());
                mCustonNativeListener.onFail(adError);
            }

            public void onAdClicked(InMobiNative inMobiNative) {
                notifyAdClicked();
            }

        });
        mCustonNativeListener = customNativeListener;

    }

    public void loadAd() {
        log(TAG, "loadad");
        mInMobiNative.load();
    }

    // Lifecycle Handlers
    @Override
    public void prepare(final View view, FrameLayout.LayoutParams layoutParams) {
        log(TAG, "prepare");
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
        log(TAG, "clear");
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
                View mMediaView = mInMobiNative.getPrimaryViewOfWidth((View) object[0], (ViewGroup) object[0], (int) object[1]);
                return mMediaView;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void destroy() {
        log(TAG, "destory");
        if (mMediaView != null && mMediaView instanceof ViewGroup) {
            ((ViewGroup) mMediaView).removeAllViews();
            mMediaView = null;

        }

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
        public void onSuccess(CustomNativeAd customNativeAd);

        public void onFail(AdError adError);
    }
}
