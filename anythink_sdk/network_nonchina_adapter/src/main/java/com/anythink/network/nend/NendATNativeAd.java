/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.nend;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anythink.core.api.ErrorCode;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;

import net.nend.android.NendAdNative;
import net.nend.android.NendAdNativeClient;
import net.nend.android.NendAdNativeMediaView;
import net.nend.android.NendAdNativeMediaViewListener;
import net.nend.android.NendAdNativeVideo;
import net.nend.android.NendAdNativeVideoListener;
import net.nend.android.NendAdNativeVideoLoader;

import java.util.ArrayList;
import java.util.List;

public class NendATNativeAd extends CustomNativeAd {

    Context mContext;
    NendAdNativeClient mClient;
    NendAdNativeVideoLoader mVideoLoader;
    LoadCallbackListener mListener;
    int mNativeType;

    NendAdNative mNendAdNative;
    NendAdNativeVideo mNendAdNativeVideo;

    protected NendATNativeAd(Context context, String apiKey, int spotId, int nativeType, LoadCallbackListener listener) {
        mContext = context.getApplicationContext();
        mListener = listener;

        mNativeType = nativeType;

        if (mNativeType == 0) {
            mClient = new NendAdNativeClient(context, spotId, apiKey);
        } else {
            mVideoLoader = new NendAdNativeVideoLoader(context, spotId, apiKey);
        }

    }

    public void loadAd() {
        if (mNativeType == 0) {
            mClient.loadAd(new NendAdNativeClient.Callback() {
                @Override
                public void onSuccess(NendAdNative nendAdNative) {
                    mNendAdNative = nendAdNative;
                    setNativeData(nendAdNative);
                    if (mListener != null) {
                        mListener.onSuccess(NendATNativeAd.this);
                    }
                    mListener = null;
                }

                @Override
                public void onFailure(NendAdNativeClient.NendError nendError) {
                    if (mListener != null) {
                        mListener.onFail(nendError.getCode() + "", nendError.getMessage());
                    }
                    mListener = null;
                }
            });
        } else {
            mVideoLoader.loadAd(new NendAdNativeVideoLoader.Callback() {
                @Override
                public void onSuccess(NendAdNativeVideo nendAdNativeVideo) {
                    mNendAdNativeVideo = nendAdNativeVideo;
                    setNativeVideoData(nendAdNativeVideo);
                    if (mListener != null) {
                        mListener.onSuccess(NendATNativeAd.this);
                    }
                    mListener = null;
                }

                @Override
                public void onFailure(int i) {
                    if (mListener != null) {
                        mListener.onFail(i + "", "");
                    }
                    mListener = null;
                }
            });
        }
    }


    private void setNativeData(NendAdNative nendAdNative) {
        setTitle(nendAdNative.getTitleText());
        setDescriptionText(nendAdNative.getContentText());
        setIconImageUrl(nendAdNative.getLogoImageUrl());
        setMainImageUrl(nendAdNative.getAdImageUrl());
        setCallToActionText(nendAdNative.getActionText());
        setAdFrom(NendAdNative.AdvertisingExplicitly.PR.toString());
        nendAdNative.setOnClickListener(new NendAdNative.OnClickListener() {
            @Override
            public void onClick(NendAdNative nendAdNative) {
                notifyAdClicked();
            }
        });
    }

    private void setNativeVideoData(NendAdNativeVideo nendAdNativeVideo) {
        setTitle(nendAdNativeVideo.getTitleText());
        setDescriptionText(nendAdNativeVideo.getDescriptionText());
        setAdFrom(nendAdNativeVideo.getAdvertiserName());
        setStarRating((double) nendAdNativeVideo.getUserRating());
        setCallToActionText(nendAdNativeVideo.getCallToActionText());
        setIconImageUrl(nendAdNativeVideo.getLogoImageUrl());

        nendAdNativeVideo.setListener(new NendAdNativeVideoListener() {
            @Override
            public void onImpression(NendAdNativeVideo nendAdNativeVideo) {
                notifyAdImpression();
            }

            @Override
            public void onClickAd(NendAdNativeVideo nendAdNativeVideo) {
                notifyAdClicked();
            }

            @Override
            public void onClickInformation(NendAdNativeVideo nendAdNativeVideo) {

            }
        });
    }

    private void getView(List<View> views, View view) {
        if (view instanceof ViewGroup && !(view instanceof NendAdNativeMediaView)) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                getView(views, child);
            }
        } else {
            if (view instanceof ImageView) {
                views.add(view);
            } else if (view instanceof Button || view instanceof TextView) {
                views.add(view);
            }
        }
    }

    @Override
    public void prepare(final View view, FrameLayout.LayoutParams layoutParams) {
        ArrayList<View> registeView = new ArrayList<>();
        getView(registeView, view);
        if (mNendAdNativeVideo != null) {
            mNendAdNativeVideo.registerInteractionViews(registeView);
        }


        if (mNendAdNative != null) {
            TextView prTextView = null;
            for (View childView : registeView) {
                if (childView instanceof TextView) {
                    if (NendAdNative.AdvertisingExplicitly.PR.toString().equals(((TextView) childView).getText().toString())) {
                        prTextView = (TextView) childView;
                    }
                }
            }
            try {
                mNendAdNative.activate(view, prTextView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        prepare(view, layoutParams);
    }

    NendAdNativeMediaView mMediaView;

    @Override
    public View getAdMediaView(Object... object) {
        if (mNendAdNativeVideo != null) {
            mMediaView = new NendAdNativeMediaView(mContext);
            mMediaView.setMediaViewListener(new NendAdNativeMediaViewListener() {
                @Override
                public void onStartPlay(NendAdNativeMediaView nendAdNativeMediaView) {
                    notifyAdVideoStart();
                }

                @Override
                public void onStopPlay(NendAdNativeMediaView nendAdNativeMediaView) {

                }

                @Override
                public void onCompletePlay(NendAdNativeMediaView nendAdNativeMediaView) {
                    notifyAdVideoEnd();
                }

                @Override
                public void onOpenFullScreen(NendAdNativeMediaView nendAdNativeMediaView) {

                }

                @Override
                public void onCloseFullScreen(NendAdNativeMediaView nendAdNativeMediaView) {
                }

                @Override
                public void onError(int i, String s) {

                }
            });
            mMediaView.setMedia(mNendAdNativeVideo);
            return mMediaView;
        }
        return super.getAdMediaView(object);
    }

    @Override
    public void clear(final View view) {
        if (mNendAdNativeVideo != null) {
            mNendAdNativeVideo.unregisterInteractionViews();
        }

        if (mMediaView != null) {
            mMediaView.setMediaViewListener(null);
            mMediaView = null;
        }
    }


    @Override
    public ViewGroup getCustomAdContainer() {
        return null;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void destroy() {
        if (mMediaView != null) {
            mMediaView.setMediaViewListener(null);
            mMediaView = null;
        }

        if (mNendAdNativeVideo != null) {
            mNendAdNativeVideo.setListener(null);
            mNendAdNativeVideo.unregisterInteractionViews();
            mNendAdNativeVideo = null;
        }
        mNendAdNative = null;
        if (mVideoLoader != null) {
            mVideoLoader.releaseLoader();
            mVideoLoader = null;
        }
        mClient = null;
        mContext = null;
        mListener = null;

    }

    interface LoadCallbackListener {
        public void onSuccess(CustomNativeAd customNativeAd);

        public void onFail(String errorCode, String errorMsg);
    }
}
