package com.anythink.splashad.api;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.api.ATSDK;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.splashad.bussiness.AdLoadManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import java.util.Map;

public class ATSplashAd {

    final String TAG = getClass().getSimpleName();
    String mUnitId;
    long mFetchDelay;

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
            mContainer.setVisibility(View.INVISIBLE);
            if (mAdLoadManager != null) {
                mAdLoadManager.release();
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
    public ATSplashAd(Activity activity, ViewGroup container, View skipView, String unitId, ATSplashAdListener listener, Map<String, String> customMap) {
        this(activity, container, skipView, unitId, listener, 5000L);
    }

    @Deprecated
    public ATSplashAd(Activity activity, ViewGroup container, View skipView, String unitId, ATSplashAdListener listener, Map<String, String> customMap, long fetchDelay) {
        this(activity, container, skipView, unitId, listener, fetchDelay);
    }

    public ATSplashAd(Activity activity, ViewGroup container, View skipView, String unitId, ATSplashAdListener listener) {
        this(activity, container, skipView, unitId, listener, 5000L);
    }

    public ATSplashAd(final Activity activity, ViewGroup container, View skipView, String unitId, ATSplashAdListener listener, long fetchDelay) {
        if (activity == null || container == null || skipView == null) {
            if (listener != null) {
                listener.onNoAdError(ErrorCode.getErrorCode(ErrorCode.exception, "", "activity, constainer or skipview could not be null!"));
            }
            Log.i(TAG, "activity, constainer or skipview could not be null!");
            return;
        }

        if (fetchDelay <= 3000L) {
            mFetchDelay = 3000L;
        } else if (fetchDelay >= 5000L) {
            mFetchDelay = 5000L;
        } else {
            mFetchDelay = fetchDelay;
        }


        mContainer = container;
        mHasDismiss = false;
        mUnitId = unitId;
        mListener = listener;

        mAdLoadManager = AdLoadManager.getInstance(activity, unitId); //new AdLoadManager(activity, container, skipView, unitId, mFetchDelay);
        mAdLoadManager.startLoadAd(mContainer, skipView, mFetchDelay, SDKContext.getInstance().getCustomMap(), mSelfListener);

        mHasReturn = false;
        /**
         * Timeout Control
         */
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                AppStrategy strategy = AppStrategyManager.getInstance(activity).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
                SDKContext.getInstance().runOnMainThreadDelayed(loadOverTimeRunnable, strategy.getPlacementTimeOut());
            }
        });


        ATSDK.apiLog(mUnitId, Const.LOGKEY.API_SPLASH, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");
    }

    public void onDestory() {
        if (mAdLoadManager != null) {
            mAdLoadManager.release();
        }

    }
}
