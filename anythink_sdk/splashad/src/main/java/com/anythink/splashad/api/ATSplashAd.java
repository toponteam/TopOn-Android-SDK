/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.api;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATMediationRequestInfo;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;
import com.anythink.splashad.bussiness.AdLoadManager;

import java.util.Map;

public class ATSplashAd {

    final String TAG = getClass().getSimpleName();
    String mPlacementId;
//    long mFetchDelay;

    AdLoadManager mAdLoadManager;

    ATSplashAdListener mListener;

    boolean mHasDismiss;

    boolean mHasReturn;

    ATSplashAdListener mSelfListener = new ATSplashAdListener() {
        @Override
        public void onAdLoaded() {
            SDKContext.getInstance().removeMainThreadRunnable(loadOverTimeRunnable);
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (!mHasReturn) {
                        mHasReturn = true;
                        if (mListener != null) {
                            mListener.onAdLoaded();
                        }
                    }
                }
            });

        }

        @Override
        public void onNoAdError(final AdError adError) {
            if (mAdLoadManager != null) {
                mAdLoadManager.setLoadFail(adError);
            }

            if (mAdLoadManager != null) {
                mAdLoadManager.releaseMediationManager();
            }

            SDKContext.getInstance().removeMainThreadRunnable(loadOverTimeRunnable);
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (!mHasReturn) {
                        mHasReturn = true;
                        if (mListener != null) {
                            mListener.onNoAdError(adError);
                        }
                    }
                }
            });
        }

        @Override
        public void onAdShow(final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onAdShow(entity);
                    }
                }
            });
        }

        @Override
        public void onAdClick(final ATAdInfo entity) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onAdClick(entity);
                    }
                }
            });
        }

        @Override
        public void onAdDismiss(final ATAdInfo entity) {
            if (mAdLoadManager != null) {
                mAdLoadManager.releaseMediationManager();
            }
            if (!mHasDismiss) {
                mHasDismiss = true;
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.onAdDismiss(entity);
                        }
                    }
                });
            }
        }

        @Override
        public void onAdTick(final long millisUtilFinished) {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onAdTick(millisUtilFinished);
                    }
                }
            });
        }
    };

    Runnable loadOverTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mContainer != null) {
                mContainer.setVisibility(View.GONE);
            }

            if (mAdLoadManager != null) {
                mAdLoadManager.releaseMediationManager();
            }

            if (!mHasReturn) {
                mHasReturn = true;
                if (mListener != null) {
                    mListener.onNoAdError(ErrorCode.getErrorCode(ErrorCode.timeOutError, "", ""));
                }
            }
        }
    };

    ViewGroup mContainer;

    @Deprecated
    public ATSplashAd(Activity activity, ViewGroup container, View skipView, String placementId, ATSplashAdListener listener, Map<String, String> customMap) {
        this(activity, container, skipView, placementId, listener, 5000L);
    }

    @Deprecated
    public ATSplashAd(Activity activity, ViewGroup container, View skipView, String placementId, ATSplashAdListener listener, Map<String, String> customMap, long fetchDelay) {
        this(activity, container, skipView, placementId, listener, fetchDelay);
    }

    @Deprecated
    public ATSplashAd(Activity activity, ViewGroup container, View skipView, String placementId, ATSplashAdListener listener) {
        this(activity, container, placementId, listener);
    }

    @Deprecated
    public ATSplashAd(final Activity activity, ViewGroup container, View skipView, String placementId, ATSplashAdListener listener, long fetchDelay) {
        this(activity, container, placementId, listener);
    }


    public ATSplashAd(final Activity activity, ViewGroup container, String placementId, ATSplashAdListener listener) {
        this(activity, container, placementId, null, null, listener);
    }

    public ATSplashAd(final Activity activity, ViewGroup container, String placementId, Map<String, Object> localMap, ATSplashAdListener listener) {
        this(activity, container, placementId, localMap, null, listener);
    }

    public ATSplashAd(final Activity activity, ViewGroup container, String placementId, ATMediationRequestInfo defaultRequestInfo, ATSplashAdListener listener) {
        this(activity, container, placementId, null, defaultRequestInfo, listener);
    }

    public ATSplashAd(final Activity activity, ViewGroup container, String placementId, Map<String, Object> localMap, ATMediationRequestInfo defaultRequestInfo, ATSplashAdListener listener) {
        if (activity == null || container == null) {
            if (listener != null) {
                listener.onNoAdError(ErrorCode.getErrorCode(ErrorCode.exception, "", "Activity, Constainer could not be null!"));
            }
            Log.i(TAG, "Activity, Constainer could not be null!");
            return;
        }

        if (TextUtils.isEmpty(placementId)) {
            if (listener != null) {
                listener.onNoAdError(ErrorCode.getErrorCode(ErrorCode.exception, "", "PlacementId could not be empty."));
            }
            Log.i(TAG, "PlacementId could not be empty.");
            return;
        }

        mContainer = container;
        mHasDismiss = false;
        mPlacementId = placementId;
        mListener = listener;

        if (defaultRequestInfo != null) {
            defaultRequestInfo.setFormat(Const.FORMAT.SPLASH_FORMAT);
        }

        if (localMap != null) {
            PlacementAdManager.getInstance().putPlacementLocalSettingMap(placementId, localMap);
        }

        mAdLoadManager = AdLoadManager.getInstance(activity, placementId); //new AdLoadManager(activity, container, skipView, placementId, mFetchDelay);
        mAdLoadManager.startLoadAd(activity, mContainer, null, defaultRequestInfo, mSelfListener);

        mHasReturn = false;
        /**
         * Timeout Control
         */
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                AppStrategy strategy = AppStrategyManager.getInstance(activity).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
                SDKContext.getInstance().runOnMainThreadDelayed(loadOverTimeRunnable, strategy.getPlacementTimeOut() == 0 ? 5000L : strategy.getPlacementTimeOut());
            }
        });


        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_SPLASH, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");
    }


    public void onDestory() {
        if (mAdLoadManager != null) {
            mAdLoadManager.releaseMediationManager();
        }

    }

    public static void checkSplashDefaultConfigList(Context context, String splashPlacementId, Map<String, Object> customMap) {
        SDKContext.getInstance().checkSplashDefaultConfig(context, splashPlacementId, customMap);
    }
}
