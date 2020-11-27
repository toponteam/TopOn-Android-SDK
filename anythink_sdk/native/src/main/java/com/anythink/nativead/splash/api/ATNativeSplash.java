/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.splash.api;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.AdError;
import com.anythink.core.common.entity.TemplateStrategy;
import com.anythink.nativead.api.ATNative;
import com.anythink.nativead.api.ATNativeNetworkListener;
import com.anythink.nativead.api.NativeAd;
import com.anythink.nativead.splash.ATNativeSplashView;

import java.util.Map;

public class ATNativeSplash {

    ATNativeSplashListener mListener;
    String mUnitId;
    long mFetchDelay;

    View mSkipView;

    boolean mIsOverLoad;

//    String mTitle;

    Handler mHandler = new Handler(Looper.getMainLooper());
    ATNativeNetworkListener nativeNetworkListener = new ATNativeNetworkListener() {
        @Override
        public void onNativeAdLoaded() {
            if (mIsOverLoad) {
                return;
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHandler.removeCallbacks(overLoadRunnable);
                    if (anythinkNative != null) {
                        NativeAd nativeAd = anythinkNative.getNativeAd();
                        if (nativeAd != null) {
                            ATNativeSplashView splashView = new ATNativeSplashView(mContainer.getContext());
                            splashView.setNativeSplashListener(mListener);
                            splashView.setDevelopSkipView(mSkipView, mFetchDelay);
                            splashView.renderAd(mContainer, nativeAd, mUnitId);

                            if (mListener != null) {
                                mListener.onAdLoaded();
                            }
                            return;
                        } else {
                        }
                    }

                    if (mListener != null) {
                        mListener.onNoAdError("Ad is empty!");
                    }
                }
            }, 20);

        }

        @Override
        public void onNativeAdLoadFail(final AdError adError) {
            if (mIsOverLoad) {
                return;
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mHandler.removeCallbacks(overLoadRunnable);
                    if (mListener != null) {
                        mListener.onNoAdError(adError.printStackTrace());
                    }
                }
            }, 20);
        }
    };

    Runnable overLoadRunnable = new Runnable() {
        @Override
        public void run() {
            mIsOverLoad = true;
            if (mListener != null) {
                mListener.onNoAdError("Ad load overtime!");
            }
        }
    };

    @Deprecated
    public ATNativeSplash(Activity activity
            , ViewGroup container
            , View skipView
            , String unitId
            , Map<String, String> customMap
            , Map<String, Object> nativeConfigMap
            , ATNativeSplashListener listener) {
        this(activity, container, skipView, unitId, null,5000L, 5000L, listener);
    }


    @Deprecated
    public ATNativeSplash(Activity activity
            , ViewGroup container
            , View skipView
            , String unitId
            , Map<String, String> customMap
            , Map<String, Object> nativeConfigMap
            , long requestTimeOut
            , long fetchDelay
            , ATNativeSplashListener listener) {

        this(activity, container, skipView, unitId, null, requestTimeOut, fetchDelay, listener);
    }


    public ATNativeSplash(Activity activity
            , ViewGroup container
            , View skipView
            , String unitId
            , Map<String, Object> localExtra
            , ATNativeSplashListener listener) {
        this(activity, container, skipView, unitId, localExtra,5000L, 5000L, listener);
    }

    public ATNativeSplash(Activity activity
            , ViewGroup container
            , View skipView
            , String unitId
            , ATNativeSplashListener listener) {
        this(activity, container, skipView, unitId, null,5000L, 5000L, listener);
    }


    ViewGroup mContainer;
    ATNative anythinkNative;
    TemplateStrategy templateStrategy;
    public ATNativeSplash(Activity activity
            , ViewGroup container
            , View skipView
            , String unitId
            , Map<String, Object> localExtra
            , long requestTimeOut
            , long fetchDelay
            , ATNativeSplashListener listener) {

        if (activity == null || container == null) {
            if (listener != null) {
                listener.onNoAdError("activity or constainer could not be null!");
            }
            return;
        }

        mIsOverLoad = false;
        if (fetchDelay <= 3000L) {
            mFetchDelay = 3000L;
        } else if (fetchDelay >= 7000L) {
            mFetchDelay = 7000L;
        } else {
            mFetchDelay = fetchDelay;
        }

        if (requestTimeOut < 0) {
            requestTimeOut = 5000L;
        }

        mContainer = container;
        mUnitId = unitId;
        mListener = listener;
        mSkipView = skipView;

        anythinkNative = new ATNative(activity.getApplicationContext(), unitId, nativeNetworkListener);
        if(localExtra != null) {
            anythinkNative.setLocalExtra(localExtra);
        }
        anythinkNative.makeAdRequest();

        mHandler.postDelayed(overLoadRunnable, requestTimeOut);
    }

}
