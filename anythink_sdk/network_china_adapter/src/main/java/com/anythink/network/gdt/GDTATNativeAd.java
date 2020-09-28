package com.anythink.network.gdt;


import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.MediaListener;
import com.qq.e.ads.nativ.MediaView;
import com.qq.e.ads.nativ.NativeADDataRef;
import com.qq.e.ads.nativ.NativeADEventListener;
import com.qq.e.ads.nativ.NativeADMediaListener;
import com.qq.e.ads.nativ.NativeMediaADData;
import com.qq.e.ads.nativ.NativeUnifiedADData;
import com.qq.e.ads.nativ.widget.NativeAdContainer;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhou on 2018/1/16.
 */

public class GDTATNativeAd extends CustomNativeAd {
    private static final String TAG = GDTATNativeAd.class.getSimpleName();
    WeakReference<Context> mContext;
    Context mApplicationContext;

    NativeMediaADData mGDTad;

    NativeUnifiedADData mUnifiedAdData; //Self-rendering 2.0

    int mVideoMuted;
    int mVideoAutoPlay;
    int mVideoDuration;


    protected GDTATNativeAd(Context context, Object gdtad, int videoMuted, int videoAutoPlay, int videoDuration) {

        mApplicationContext = context.getApplicationContext();
        mContext = new WeakReference<>(context);

        mVideoMuted = videoMuted;
        mVideoAutoPlay = videoAutoPlay;
        mVideoDuration = videoDuration;

        if (gdtad instanceof NativeMediaADData) {
            mGDTad = (NativeMediaADData) gdtad;
            setAdData(mGDTad);
        }

        if (gdtad instanceof NativeUnifiedADData) {
            mUnifiedAdData = (NativeUnifiedADData) gdtad;
            setAdData(mUnifiedAdData);
        }

    }


    public String getCallToACtion(Object ad) {
        boolean isapp = false;
        int status = 0, pro = 0;
        if (ad instanceof NativeMediaADData) {

            isapp = ((NativeMediaADData) ad).isAPP();
            status = ((NativeMediaADData) ad).getAPPStatus();
            pro = ((NativeMediaADData) ad).getProgress();
        }

        if (ad instanceof NativeADDataRef) {
            isapp = ((NativeADDataRef) ad).isAPP();
            status = ((NativeADDataRef) ad).getAPPStatus();
            pro = ((NativeADDataRef) ad).getProgress();
        }

        if (ad instanceof NativeUnifiedADData) {
            isapp = ((NativeUnifiedADData) ad).isAppAd();
            status = ((NativeUnifiedADData) ad).getAppStatus();
            pro = ((NativeUnifiedADData) ad).getProgress();
        }


        if (!isapp) {
            return "浏览";
        }
        switch (status) {
            case 0:
            case 4:
            case 16:
                return "下载";
            case 1:
                return "启动";
            case 2:
                return "更新";
            case 8:
                return "安装";
            default:
                return "浏览";
        }
    }


    private void setAdData(NativeMediaADData gdtad) {
        setTitle(gdtad.getTitle());
        setDescriptionText(gdtad.getDesc());

        setIconImageUrl(gdtad.getIconUrl());
        setStarRating((double) gdtad.getAPPScore());

        setCallToActionText(getCallToACtion(gdtad));

        setMainImageUrl(gdtad.getImgUrl());

        setImageUrlList(gdtad.getImgList());

        if (gdtad.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
            mAdSourceType = VIDEO_TYPE;
        } else {
            mAdSourceType = IMAGE_TYPE;
        }

    }

    private void setAdData(NativeUnifiedADData unifiedADData) {
        setTitle(unifiedADData.getTitle());
        setDescriptionText(unifiedADData.getDesc());

        setIconImageUrl(unifiedADData.getIconUrl());
        setStarRating((double) unifiedADData.getAppScore());

        setCallToActionText(getCallToACtion(unifiedADData));

        setMainImageUrl(unifiedADData.getImgUrl());

        setImageUrlList(unifiedADData.getImgList());

        if (unifiedADData.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
            mAdSourceType = VIDEO_TYPE;
        } else {
            mAdSourceType = IMAGE_TYPE;
        }

        unifiedADData.setNativeAdEventListener(new NativeADEventListener() {
            @Override
            public void onADExposed() {

            }

            @Override
            public void onADClicked() {
                notifyAdClicked();
            }

            @Override
            public void onADError(AdError adError) {

            }

            @Override
            public void onADStatusChanged() {

            }
        });
    }


    MediaView mMediaView;

    boolean hasBeenPlay = false;

    @Override
    public View getAdMediaView(Object... object) {
        if (mGDTad != null) { //Video advertising
            if (mGDTad.getAdPatternType() != AdPatternType.NATIVE_VIDEO) {
                return super.getAdMediaView(object);
            }

            mMediaView = new MediaView(mApplicationContext);
            mMediaView.setBackgroundColor(0xff000000);
            ViewGroup.LayoutParams _params = mMediaView.getLayoutParams();
            if (_params == null) {
                _params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            mMediaView.setLayoutParams(_params);
            mMediaView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view) {
                    try {
                        if (!hasBeenPlay) {
                            hasBeenPlay = true;
                            if (mGDTad.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                                mGDTad.setVolumeOn(true);
                                mGDTad.play();
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onViewDetachedFromWindow(View view) {
                }
            });

            /** set listener */
            mGDTad.setMediaListener(new MediaListener() {

                @Override
                public void onVideoReady(long videoDuration) {
                    Log.i(TAG, "onVideoReady, videoDuration = " + videoDuration);

                }

                @Override
                public void onVideoStart() {
                    Log.i(TAG, "onVideoStart");
                    notifyAdVideoStart();

                }

                @Override
                public void onVideoPause() {
                    Log.i(TAG, "onVideoPause");

                }

                @Override
                public void onVideoComplete() {
                    Log.i(TAG, "onVideoComplete");
                    notifyAdVideoEnd();
                }

                @Override
                public void onVideoError(AdError adError) {
                    Log.i(TAG, String.format("onVideoError, errorCode: %d, errorMsg: %s",
                            adError.getErrorCode(), adError.getErrorMsg()));
                }

                @Override
                public void onReplayButtonClicked() {
                    Log.i(TAG, "onReplayButtonClicked");
                }

                @Override
                public void onADButtonClicked() {
                    Log.i(TAG, "onADButtonClicked");
                }

                @Override
                public void onFullScreenChanged(boolean inFullScreen) {
                    Log.i(TAG, "onFullScreenChanged, inFullScreen = " + inFullScreen);
                }
            });

            return mMediaView;
        }

        if (mUnifiedAdData != null) {
            if (mUnifiedAdData.getAdPatternType() != AdPatternType.NATIVE_VIDEO) {
                return super.getAdMediaView(object);
            }

            mMediaView = new MediaView(mApplicationContext);
            mMediaView.setBackgroundColor(0xff000000);
            ViewGroup.LayoutParams _params = mMediaView.getLayoutParams();
            if (_params == null) {
                _params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            mMediaView.setLayoutParams(_params);


            return mMediaView;
        }

        return super.getAdMediaView(object);
    }

    @Override
    public boolean isNativeExpress() {
        return false;
    }

    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {


        if (mGDTad != null) {
            mGDTad.onExposured(view);
            registerView(view);
            try {
                mGDTad.bindView(mMediaView, true);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }


        if (mUnifiedAdData != null && mContainer != null) {
            List<View> childView = new ArrayList<>();
            fillChildView(view, childView);
            mUnifiedAdData.bindAdToView(view.getContext(), mContainer, layoutParams, childView);
            try {
                mUnifiedAdData.bindMediaView(mMediaView, new VideoOption.Builder()
                        .setAutoPlayMuted(mVideoMuted == 1).setAutoPlayPolicy(mVideoAutoPlay).build(), new NativeADMediaListener() {
                    @Override
                    public void onVideoInit() {
                    }

                    @Override
                    public void onVideoLoading() {
                    }

                    @Override
                    public void onVideoReady() {
                    }

                    @Override
                    public void onVideoLoaded(int i) {
                    }

                    @Override
                    public void onVideoStart() {
                        notifyAdVideoStart();
                    }

                    @Override
                    public void onVideoPause() {
                    }

                    @Override
                    public void onVideoResume() {
                    }

                    @Override
                    public void onVideoCompleted() {
                        notifyAdVideoEnd();
                    }

                    @Override
                    public void onVideoError(AdError adError) {
                    }

                    @Override
                    public void onVideoStop() {

                    }

                    @Override
                    public void onVideoClicked() {

                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        if (mGDTad != null) {
            mGDTad.onExposured(view);
            registerView(view);
            try {
                mGDTad.bindView(mMediaView, true);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (mUnifiedAdData != null && mContainer != null) {
            mUnifiedAdData.bindAdToView(view.getContext(), mContainer, layoutParams, clickViewList);
            try {
                mUnifiedAdData.bindMediaView(mMediaView, new VideoOption.Builder()
                        .setAutoPlayMuted(mVideoMuted == 1).setAutoPlayPolicy(mVideoAutoPlay).build(), new NativeADMediaListener() {
                    @Override
                    public void onVideoInit() {
                    }

                    @Override
                    public void onVideoLoading() {
                    }

                    @Override
                    public void onVideoReady() {
                    }

                    @Override
                    public void onVideoLoaded(int i) {
                    }

                    @Override
                    public void onVideoStart() {
                        notifyAdVideoStart();
                    }

                    @Override
                    public void onVideoPause() {
                    }

                    @Override
                    public void onVideoResume() {
                    }

                    @Override
                    public void onVideoCompleted() {
                        notifyAdVideoEnd();
                    }

                    @Override
                    public void onVideoError(AdError adError) {
                    }

                    @Override
                    public void onVideoStop() {

                    }

                    @Override
                    public void onVideoClicked() {

                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }


    }

    //Self-rendering 2.0 must be used
    NativeAdContainer mContainer;

    public ViewGroup getCustomAdContainer() {
        if (mUnifiedAdData != null) {
            mContainer = new NativeAdContainer(mApplicationContext);
        }
        return mContainer;
    }


    private void registerView(View view) {

        if (view instanceof ViewGroup && view != mMediaView) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                registerView(child);
            }
        } else {
            if (mGDTad != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View pView) {
                        mGDTad.onClicked(pView);
                        notifyAdClicked();
                    }
                });
            }
        }
    }

    private void fillChildView(View parentView, List<View> childViews) {
        if (parentView instanceof ViewGroup && parentView != mMediaView) {
            ViewGroup viewGroup = (ViewGroup) parentView;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                fillChildView(child, childViews);
            }
        } else {
            childViews.add(parentView);
        }
    }

    private void unregisterView(View view) {
        if (view instanceof ViewGroup && view != mMediaView) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                unregisterView(child);
            }
        } else {
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                String cat = textView.getText().toString();
                if (mGDTad != null) {
                    if (cat.equals(getCallToACtion(mGDTad))) {

                        textView.setOnClickListener(null);
                    }
                }
            }
        }
    }

    @Override
    public void clear(View view) {
        super.clear(view);
        unregisterView(view);
        onPause();
        mMediaView = null;
        mContainer = null;
    }

    @Override
    public void onPause() {
        if (mGDTad != null && mGDTad.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
            mGDTad.stop();
        }

        if (mUnifiedAdData != null) {
            mUnifiedAdData.pauseVideo();
        }
    }

    @Override
    public void onResume() {

        if (mGDTad != null && mGDTad.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
            mGDTad.resume();
        }

        if (mUnifiedAdData != null) {
            mUnifiedAdData.resume();
            mUnifiedAdData.resumeVideo();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mGDTad != null) {
            mGDTad.stop();
            mGDTad.setMediaListener(null);
            mGDTad.destroy();
        }

        if (mUnifiedAdData != null) {
            mUnifiedAdData.setNativeAdEventListener(null);
            mUnifiedAdData.destroy();
            mUnifiedAdData = null;
        }
        mMediaView = null;

        mApplicationContext = null;
        if (mContext != null) {
            mContext.clear();
            mContext = null;
        }

        if (mContainer != null) {
            mContainer.removeAllViews();
            mContainer = null;
        }
    }
}
