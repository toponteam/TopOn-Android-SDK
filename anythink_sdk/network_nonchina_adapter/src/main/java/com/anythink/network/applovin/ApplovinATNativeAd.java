package com.anythink.network.applovin;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.network.applovin.view.PlayerView;
import com.anythink.network.applovin.view.VideoFeedsPlayerListener;
import com.applovin.nativeAds.AppLovinNativeAd;
import com.applovin.sdk.AppLovinSdk;

import java.util.List;

/**
 * Created by Z on 2018/1/11.
 */

public class ApplovinATNativeAd extends CustomNativeAd {


    private static final String TAG = ApplovinATNativeAd.class.getSimpleName();
    AppLovinNativeAd mNativeAd;
    AppLovinSdk mApplovinSdk;
    Context mContext;

    public ApplovinATNativeAd(Context context
            , AppLovinNativeAd nativeAd, AppLovinSdk appLovinSdk) {
        mContext = context.getApplicationContext();
        mNativeAd = nativeAd;
        mApplovinSdk = appLovinSdk;
        setAdData(mNativeAd);
    }

    // Lifecycle Handlers
    @Override
    public void prepare(final View view, FrameLayout.LayoutParams layoutParams) {
        if (view == null) {
            return;
        }
        registerView(view, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNativeAd != null) {
                    mNativeAd.launchClickTarget(view.getContext());
                    notifyAdClicked();
                }
            }
        });
        if (mNativeAd != null) {
            mNativeAd.trackImpression();
        }
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {

        for (View childView : clickViewList) {
            if (childView != null) {
                childView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mNativeAd != null) {
                            mNativeAd.launchClickTarget(view.getContext());
                            notifyAdClicked();
                        }
                    }
                });
            }
        }
        if (mNativeAd != null) {
            mNativeAd.trackImpression();
        }
    }

    private void registerView(View view, View.OnClickListener clickListener) {
        if (view instanceof PlayerView) {
            return;
        }
        if (view instanceof ViewGroup) {
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
        if (view instanceof PlayerView) {
            ((PlayerView) view).release();
            return;
        }
        if (view instanceof ViewGroup) {
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

    }

    @Override
    public void destroy() {
        if (mPlayerView != null) {
            mPlayerView.setListener(null);
            mPlayerView.release();
            mPlayerView = null;
        }
        mApplovinSdk = null;
        mNativeAd = null;
        mContext = null;
    }

    private void setAdData(AppLovinNativeAd nativeAd) {
        mNativeAd = nativeAd;
        setTitle(nativeAd.getTitle());
        setDescriptionText(nativeAd.getDescriptionText());

        setIconImageUrl(nativeAd.getIconUrl());
        setStarRating((double) nativeAd.getStarRating());

        setCallToActionText(nativeAd.getCtaText());
        setMainImageUrl(nativeAd.getImageUrl());

        Log.d(TAG, "setAdData---->" + nativeAd.getVideoUrl());
        setVideoUrl(nativeAd.getVideoUrl());

        if (!TextUtils.isEmpty(nativeAd.getVideoUrl())) {
            mAdSourceType = VIDEO_TYPE;
        } else {
            mAdSourceType = IMAGE_TYPE;
        }
    }

    PlayerView mPlayerView;

    @Override
    public View getAdMediaView(Object... object) {
        try {

            if (getVideoUrl() == null) {
                return null;
            } else {
                if (mPlayerView != null) {
                    mPlayerView.setListener(null);
                    mPlayerView.release();
                    mPlayerView = null;
                }
                mPlayerView = new PlayerView(this.mContext);
                mPlayerView.initVFPData(getVideoUrl(), false, true, new VideoFeedsPlayerListener() {
                    @Override
                    public void onPlayStarted(int allDuration) {

                        mApplovinSdk.getPostbackService().dispatchPostbackAsync(mNativeAd.getVideoStartTrackingUrl(), null);
                        notifyAdVideoStart();
                    }

                    @Override
                    public void onPlayCompleted() {
                        mApplovinSdk.getPostbackService().dispatchPostbackAsync(mNativeAd.getVideoEndTrackingUrl(100, true), null);
                        notifyAdVideoEnd();
                    }

                    @Override
                    public void onPlayError(String errorStr) {

                        notifyAdVideoEnd();
                    }

                    @Override
                    public void onPlayProgress(int curPlayPosition, int allDuration) {
                        notifyAdVideoPlayProgress(curPlayPosition);
                    }

                    @Override
                    public void OnBufferingStart(String bufferMsg) {

                    }

                    @Override
                    public void OnBufferingEnd() {

                    }

                    @Override
                    public void onPlaySetDataSourceError(String errorStr) {

                    }

                    @Override
                    public void onPalyRestart(int curPlayPosition, int allDuration) {

                    }

                    @Override
                    public void onPalyPause(int curPlayPosition) {

                    }

                    @Override
                    public void onPalyResume(int curPlayPosition) {

                    }

                    @Override
                    public void onSoundStat(boolean soundopen) {

                    }

                    @Override
                    public void onPlayClose() {

                    }

                    @Override
                    public void onAdClicked() {

                    }

                    @Override
                    public void closeADView() {

                    }

                    @Override
                    public void onInitCallBack(boolean intiState) {

                    }
                });
                mPlayerView.playVideo();
                return mPlayerView;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    boolean mIsAutoPlay;

    public void setIsAutoPlay(boolean isAutoPlay) {
        mIsAutoPlay = isAutoPlay;
    }


}
