package com.anythink.network.huawei;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.VideoConfiguration;
import com.huawei.hms.ads.VideoOperator;
import com.huawei.hms.ads.nativead.MediaView;
import com.huawei.hms.ads.nativead.NativeAd;
import com.huawei.hms.ads.nativead.NativeAdConfiguration;
import com.huawei.hms.ads.nativead.NativeAdLoader;
import com.huawei.hms.ads.nativead.NativeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HuaweiATNativeAd extends CustomNativeAd {
    NativeAd mNativeAd;
    Context mContext;
    String mAdId;

    NativeView mNativeAdView;

    public HuaweiATNativeAd(Context context, String adId) {
        mContext = context.getApplicationContext();
        mAdId = adId;
    }

    protected void loadAd(Map<String, Object> serverExtras, final LoadCallbackListener callbackListener) {
        boolean isMuted = true;

        int mediaViewAspect = NativeAdConfiguration.MediaAspect.ASPECT_ANY;
        int direction = NativeAdConfiguration.Direction.LANDSCAPE;

        if (serverExtras.containsKey("video_muted")) {
            isMuted = Integer.parseInt(serverExtras.get("video_muted").toString()) == 0;
        }

        if (serverExtras.containsKey("media_ratio")) {
            int mediaRaioServer = Integer.parseInt(serverExtras.get("media_ratio").toString());
            switch (mediaRaioServer) {
                case 0:
                    mediaViewAspect = NativeAdConfiguration.MediaAspect.ASPECT_UNKNOWN;
                    break;
                case 1:
                    mediaViewAspect = NativeAdConfiguration.MediaAspect.ASPECT_ANY;
                    break;
                case 2:
                    mediaViewAspect = NativeAdConfiguration.MediaAspect.ASPECT_LANDSCAPE;
                    break;
                case 3:
                    mediaViewAspect = NativeAdConfiguration.MediaAspect.ASPECT_PORTRAIT;
                    break;
                case 4:
                    mediaViewAspect = NativeAdConfiguration.MediaAspect.ASPECT_SQUARE;
                    break;
            }
        }

        if (serverExtras.containsKey("orientation")) {
            int directionServer = Integer.parseInt(serverExtras.get("orientation").toString());
            switch (directionServer) {
                case 0:
                    direction = NativeAdConfiguration.Direction.ANY;
                    break;
                case 1:
                    direction = NativeAdConfiguration.Direction.PORTRAIT;
                    break;
                case 2:
                    direction = NativeAdConfiguration.Direction.LANDSCAPE;
                    break;
            }
        }


        NativeAdLoader.Builder builder = new NativeAdLoader.Builder(mContext, mAdId);
        builder.setNativeAdLoadedListener(new NativeAd.NativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                setAdData(nativeAd);
                if (callbackListener != null) {
                    callbackListener.onSuccess(HuaweiATNativeAd.this);
                }
            }
        }).setAdListener(new AdListener() {
            @Override
            public void onAdFailed(int errorCode) {
                if (callbackListener != null) {
                    callbackListener.onFail(String.valueOf(errorCode), "");
                }
            }

            @Override
            public void onAdClicked() {
                notifyAdClicked();
            }
        });

        VideoConfiguration videoConfiguration = new VideoConfiguration.Builder()
                // Set whether to play video assets in mute mode. The default value is true.
                .setStartMuted(isMuted)
                .build();

        NativeAdConfiguration adConfiguration = new NativeAdConfiguration.Builder()
                .setVideoConfiguration(videoConfiguration)
                .setMediaAspect(mediaViewAspect)
                .setMediaDirection(direction)
                .build();

        builder.setNativeAdOptions(adConfiguration);

        NativeAdLoader nativeAdLoader = builder.build();
        nativeAdLoader.loadAd(new AdParam.Builder().build());
    }

    private void setAdData(NativeAd nativeAd) {
        mNativeAd = nativeAd;

        setTitle(mNativeAd.getTitle());
        if (mNativeAd.getIcon() != null) {
            setIconImageUrl(mNativeAd.getIcon().getUri().toString());
        }

        setDescriptionText(mNativeAd.getDescription());
        setAdFrom(mNativeAd.getAdSource());
        setCallToActionText(mNativeAd.getCallToAction());

        if (mNativeAd != null && mNativeAd.getImages() != null && mNativeAd.getImages().size() > 0 && mNativeAd.getImages().get(0).getUri() != null) {
            setMainImageUrl(mNativeAd.getImages().get(0).getUri().toString());
        }

        // Obtain a video controller.
        VideoOperator videoOperator = nativeAd.getVideoOperator();

        // Check whether a native ad contains video materials.
        if (videoOperator.hasVideo()) {
            mAdSourceType = VIDEO_TYPE;
            // Add a video lifecycle event listener.
            videoOperator.setVideoLifecycleListener(new VideoOperator.VideoLifecycleListener() {
                @Override
                public void onVideoStart() {
                    notifyAdVideoStart();
                }

                @Override
                public void onVideoPlay() {
                }

                @Override
                public void onVideoPause() {
                }

                @Override
                public void onVideoEnd() {
                    notifyAdVideoEnd();
                }

                @Override
                public void onVideoMute(boolean var1) {
                }
            });
        } else {
            mAdSourceType = IMAGE_TYPE;
        }

        mNativeAdView = new NativeView(mContext);
        mNativeAdView.setNativeAd(nativeAd);
    }

    @Override
    public View getAdMediaView(Object... object) {
        if (mNativeAd != null) {
            MediaView mediaView = new MediaView(mContext);
            mNativeAdView.setMediaView(mediaView);
            mNativeAdView.getMediaView().setMediaContent(mNativeAd.getMediaContent());
            return mediaView;
        }
        return null;
    }

    @Override
    public ViewGroup getCustomAdContainer() {
        return mNativeAdView;
    }

    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {
        if (mNativeAd != null && mNativeAd != null) {
            mNativeAdView.setNativeAd(mNativeAd);
        }

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
        if (mNativeAd != null && mNativeAd != null) {
            mNativeAdView.setNativeAd(mNativeAd);
        }

        List<View> imageViews = new ArrayList<View>();
        for (View childView : clickViewList) {
            if (childView instanceof ImageView) {
                imageViews.add(childView);
            } else if (childView instanceof Button || childView instanceof TextView) {
                String text = ((TextView) childView).getText().toString();
                if (mNativeAd != null && mNativeAdView != null) {
                    if (text.equals(mNativeAd.getTitle())) {
                        mNativeAdView.setTitleView(childView);
                    }
                    if (text.equals(mNativeAd.getDescription())) {
                        mNativeAdView.setDescriptionView(childView);
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
        if (view instanceof ViewGroup && !(view instanceof MediaView)) {
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
                    if (text.equals(mNativeAd.getTitle())) {
                        mNativeAdView.setTitleView(view);
                    }
                    if (text.equals(mNativeAd.getDescription())) {
                        mNativeAdView.setDescriptionView(view);
                    }
                    if (text.equals(mNativeAd.getCallToAction())) {
                        mNativeAdView.setCallToActionView(view);
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {
        if (mNativeAdView != null) {
            mNativeAdView.destroy();
            mNativeAdView = null;
        }
        mContext = null;
        if (mNativeAd != null) {
            mNativeAd.destroy();
            mNativeAd = null;
        }
    }

    protected interface LoadCallbackListener {
        void onSuccess(CustomNativeAd customNativeAd);

        void onFail(String errorCode, String errorMsg);
    }
}
