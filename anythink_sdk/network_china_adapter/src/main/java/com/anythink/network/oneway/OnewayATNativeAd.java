package com.anythink.network.oneway;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;

import java.util.List;

import mobi.oneway.export.AdListener.feed.OWFeedAdEventListener;
import mobi.oneway.export.AdListener.feed.OWFeedVideoAdListener;
import mobi.oneway.export.enums.OnewaySdkError;
import mobi.oneway.export.feed.IFeedAd;

class OnewayATNativeAd extends CustomNativeAd {

    public static final String TAG = OnewayATNativeAd.class.getSimpleName();
    IFeedAd iFeedAd;

    public OnewayATNativeAd(IFeedAd iFeedAd) {
        this.iFeedAd = iFeedAd;
        setData();
    }

    private void setData() {
        if (iFeedAd != null) {
            setTitle(iFeedAd.getTitle());
            List<String> images = iFeedAd.getImages();
            setImageUrlList(images);
            if(images.size() > 0) {
                setMainImageUrl(images.get(0));
            }
            setIconImageUrl(iFeedAd.getIconImage());
        }
    }

    @Override
    public View getAdMediaView(Object... object) {
        if (iFeedAd != null) {
            return iFeedAd.getVideoView();
        }
        return null;
    }

    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {

        if(iFeedAd != null && view instanceof ViewGroup) {
            iFeedAd.handleAdEvent(((ViewGroup) view), new OWFeedAdEventListener() {
                @Override
                public void onExposured(IFeedAd iFeedAd) {

                }

                @Override
                public void onClicked(IFeedAd iFeedAd) {
                    notifyAdClicked();
                }
            });

            iFeedAd.setVideoAdListener(new OWFeedVideoAdListener() {
                @Override
                public void onVideoLoad(IFeedAd iFeedAd) {

                }

                @Override
                public void onVideoError(OnewaySdkError onewaySdkError, String s) {
                    Log.e(TAG, "onVideoError: " + onewaySdkError.name() + ", " + s);
                }

                @Override
                public void onVideoPlay(IFeedAd iFeedAd) {

                }
            });
        }
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        this.prepare(view, layoutParams);
    }

    @Override
    public void destroy() {
        if (iFeedAd != null) {
            iFeedAd.setVideoAdListener(null);
            iFeedAd = null;
        }
    }
}
