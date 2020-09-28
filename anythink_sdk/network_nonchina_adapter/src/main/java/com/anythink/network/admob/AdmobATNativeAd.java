package com.anythink.network.admob;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhou on 2018/1/16.
 */

public class AdmobATNativeAd extends CustomNativeAd implements UnifiedNativeAd.OnUnifiedNativeAdLoadedListener {

    private final String TAG = AdmobATNativeAd.class.getSimpleName();

    Context mContext;
    LoadCallbackListener mCustomNativeListener;
    String mUnitId;

    MediaView mMediaView;

    int clickType = 0;

    UnifiedNativeAd mNativeAd;

    int mediaRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_UNKNOWN;

    public AdmobATNativeAd(Context context, String unitId, LoadCallbackListener customNativeListener
            , Map<String, Object> localExtras) {

        mContext = context.getApplicationContext();
        mCustomNativeListener = customNativeListener;
        mUnitId = unitId;

        clickType = 0;
    }

    public AdmobATNativeAd(Context context, String ratio, String unitId, LoadCallbackListener customNativeListener
            , Map<String, Object> localExtras) {

        this(context, unitId, customNativeListener, localExtras);
        if (!TextUtils.isEmpty(ratio)) {
            switch (ratio) {
                case "1":
                    this.mediaRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY;
                    break;
                case "2":
                    this.mediaRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE;
                    break;
                case "3":
                    this.mediaRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT;
                    break;
                case "4":
                    this.mediaRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE;
                    break;
                default:
                    this.mediaRatio = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_UNKNOWN;
                    break;
            }
        }
    }


    public void loadAd(final Context context, final Bundle gdprBundle) {

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .setMediaAspectRatio(mediaRatio)
                .build();


        AdLoader adLoader = new AdLoader.Builder(context, mUnitId)
                .forUnifiedNativeAd(AdmobATNativeAd.this)
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        if (mCustomNativeListener != null) {
                            mCustomNativeListener.onFail(String.valueOf(errorCode), "");
                        }
                        mCustomNativeListener = null;
                    }

                    @Override
                    public void onAdClicked() {
                        if (clickType == 0) {
                            clickType = 1;
                        }
                        if (clickType == 1) {
                            notifyAdClicked();
                        }
                    }

                    @Override
                    public void onAdImpression() {
                    }

                    @Override
                    public void onAdLeftApplication() {
                        if (clickType == 0) {
                            clickType = 2;
                        }
                        if (clickType == 2) {
                            notifyAdClicked();
                        }
                    }
                })
                .withNativeAdOptions(adOptions).build();


        adLoader.loadAd(new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, gdprBundle).build());

    }

    @Override
    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
        mNativeAd = unifiedNativeAd;

        setTitle(mNativeAd.getHeadline());
        setDescriptionText(mNativeAd.getBody());
        if (mNativeAd != null && mNativeAd.getIcon() != null && mNativeAd.getIcon().getUri() != null) {
            setIconImageUrl(mNativeAd.getIcon().getUri().toString());
        }
        if (mNativeAd != null && mNativeAd.getImages() != null && mNativeAd.getImages().size() > 0 && mNativeAd.getImages().get(0).getUri() != null) {
            setMainImageUrl(mNativeAd.getImages().get(0).getUri().toString());
        }
        setCallToActionText(mNativeAd.getCallToAction());
        setStarRating(mNativeAd.getStarRating() == null ? 5.0 : mNativeAd.getStarRating());
        setAdFrom(mNativeAd.getStore());

        VideoController vc = mNativeAd.getVideoController();
        if (vc.hasVideoContent()) {
            mAdSourceType = VIDEO_TYPE;
        } else {
            mAdSourceType = IMAGE_TYPE;
        }
        if (mCustomNativeListener != null) {
            mCustomNativeListener.onSuccess(AdmobATNativeAd.this);
        }
        mCustomNativeListener = null;

    }

    private UnifiedNativeAdView createAdView() {
        UnifiedNativeAdView unifiedNativeAdView = new UnifiedNativeAdView(mContext);
        VideoController videoController = mNativeAd.getVideoController();
        if (videoController != null && videoController.hasVideoContent()) {
            videoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoStart() {
                    super.onVideoStart();
                    notifyAdVideoStart();

                }

                @Override
                public void onVideoPlay() {
                    super.onVideoPlay();
                }

                @Override
                public void onVideoPause() {
                    super.onVideoPause();
                }

                @Override
                public void onVideoEnd() {
                    super.onVideoEnd();
                    notifyAdVideoEnd();
                }

                @Override
                public void onVideoMute(boolean b) {
                    super.onVideoMute(b);

                }
            });
        }
        unifiedNativeAdView.setNativeAd(mNativeAd);
        return unifiedNativeAdView;

    }

    UnifiedNativeAdView mNativeAdView;

    @Override
    public ViewGroup getCustomAdContainer() {
        mNativeAdView = createAdView();
        return mNativeAdView;
    }

    @Override
    public View getAdMediaView(Object... object) {
        mMediaView = new MediaView(mContext);
        if (mNativeAdView != null) {
            mNativeAdView.setMediaView(mMediaView);
            if (mNativeAd != null) {
                mNativeAdView.setNativeAd(mNativeAd);
            }
        }
        return mMediaView;
    }


    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {

        List<View> imageViews = new ArrayList<View>();

        getView(imageViews, mNativeAdView);

        for (int i = 0; i < imageViews.size(); i++) {
            if (i == 0) {
                mNativeAdView.setIconView(imageViews.get(i));
            }
            if (i == 1) {
                mNativeAdView.setImageView(imageViews.get(i));
                break;
            }
        }

    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        List<View> imageViews = new ArrayList<View>();
        for (View childView : clickViewList) {
            if (childView instanceof ImageView) {
                imageViews.add(childView);
            } else if (childView instanceof Button || childView instanceof TextView) {
                String text = ((TextView) childView).getText().toString();
                if (mNativeAd != null && mNativeAdView != null) {
                    if (text.equals(mNativeAd.getHeadline())) {
                        mNativeAdView.setHeadlineView(childView);
                    }
                    if (text.equals(mNativeAd.getBody())) {
                        mNativeAdView.setBodyView(childView);
                    }
                    if (text.equals(mNativeAd.getCallToAction())) {
                        mNativeAdView.setCallToActionView(childView);
                    }
                }
            }
        }

        for (int i = 0; i < imageViews.size(); i++) {
            if (i == 0) {
                mNativeAdView.setIconView(imageViews.get(i));
            }
            if (i == 1) {
                mNativeAdView.setImageView(imageViews.get(i));
                break;
            }
        }
    }

    private void getView(List<View> imageViews, View view) {
        if (view instanceof ViewGroup && view != mMediaView) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                getView(imageViews, child);
            }
        } else {
            if (view instanceof ImageView) {
                imageViews.add(view);
            } else if (view instanceof Button || view instanceof TextView) {
                String text = ((TextView) view).getText().toString();
                if (mNativeAd != null && mNativeAdView != null) {
                    if (text.equals(mNativeAd.getHeadline())) {
                        mNativeAdView.setHeadlineView(view);
                    }
                    if (text.equals(mNativeAd.getBody())) {
                        mNativeAdView.setBodyView(view);
                    }
                    if (text.equals(mNativeAd.getCallToAction())) {
                        mNativeAdView.setCallToActionView(view);
                    }
                }
            }
        }
    }

    @Override
    public void clear(View view) {
        if (mNativeAdView != null) {
            mNativeAdView.destroy();
            mNativeAdView = null;
        }
        mMediaView = null;
    }

    @Override
    public void destroy() {
        if (mNativeAdView != null) {
            mNativeAdView.destroy();
            mNativeAdView = null;
        }
        mMediaView = null;
        mCustomNativeListener = null;
        mContext = null;
        if (mNativeAd != null) {
            mNativeAd.destroy();
            mNativeAd = null;
        }
    }

    boolean mIsAutoPlay;

    public void setIsAutoPlay(boolean isAutoPlay) {
        mIsAutoPlay = isAutoPlay;
    }

    protected interface LoadCallbackListener {
        void onSuccess(CustomNativeAd customNativeAd);

        void onFail(String errorCode, String errorMsg);
    }

}
