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
import android.view.ViewGroup;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATAdStatusInfo;
import com.anythink.core.api.ATMediationRequestInfo;
import com.anythink.core.api.ATNetworkConfirmInfo;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;
import com.anythink.splashad.bussiness.AdEventListener;
import com.anythink.splashad.bussiness.AdLoadListener;
import com.anythink.splashad.bussiness.AdLoadManager;

import java.lang.ref.WeakReference;
import java.util.Map;

public class ATSplashAd {

    final String TAG = getClass().getSimpleName();
    String mPlacementId;

    AdLoadManager mAdLoadManager;

    ATSplashAdListener mListener;

    ATMediationRequestInfo mDefaultRequestInfo;

    Context mContext;

    WeakReference<Activity> mActivityWeakRef;

    int mFetchAdTimeout;

    public ATSplashAd(Context context, String placementId, ATSplashAdListener listener) {
        this(context, placementId, null, listener, 0);
    }

    public ATSplashAd(Context context, String placementId, ATMediationRequestInfo defaultRequestInfo, ATSplashAdListener listener) {
        this(context, placementId, defaultRequestInfo, listener, 0);
    }

    public ATSplashAd(Context context, String placementId, ATMediationRequestInfo defaultRequestInfo, ATSplashAdListener listener, int fetchAdTimeout) {
        mContext = context.getApplicationContext();
        mPlacementId = placementId;
        mListener = listener;
        mDefaultRequestInfo = defaultRequestInfo;

        mFetchAdTimeout = fetchAdTimeout;

        if (context instanceof Activity) {
            mActivityWeakRef = new WeakReference<>((Activity) context);
        }


        if (mDefaultRequestInfo != null) {
            mDefaultRequestInfo.setFormat(Const.FORMAT.SPLASH_FORMAT);
        }

        mAdLoadManager = AdLoadManager.getInstance(context, placementId); //new AdLoadManager(activity, container, skipView, placementId, mFetchDelay);

    }


    /**
     * Mediation Setting Map
     *
     * @param map
     */
    public void setLocalExtra(Map<String, Object> map) {
        PlacementAdManager.getInstance().putPlacementLocalSettingMap(mPlacementId, map);
    }

    public void loadAd() {
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_SPLASH, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");

        /**
         * Timeout Control
         */
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                /**Check Developer having set Timeout**/
                int timeout = mFetchAdTimeout;
                if (timeout <= 0) {
                    //Use Strategy or Default
                    AppStrategy strategy = AppStrategyManager.getInstance(mContext).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
                    timeout = strategy.getPlacementTimeOut() == 0 ? 5000 : (int) strategy.getPlacementTimeOut();
                }

                Activity activity = mActivityWeakRef != null ? mActivityWeakRef.get() : null;

                AdLoadListener mAdLoadListener = new AdLoadListener() {
                    @Override
                    public void onAdLoaded(String requestId) {
                        SDKContext.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mListener != null) {
                                    mListener.onAdLoaded();
                                }
                            }
                        });
                    }

                    @Override
                    public void onNoAdError(String requestId, final AdError adError) {
                        if (mAdLoadManager != null) {
                            mAdLoadManager.setLoadFail(adError);
                        }

                        SDKContext.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mListener != null) {
                                    mListener.onNoAdError(adError);
                                }
                            }
                        });
                    }

                    @Override
                    public void onTimeout(final String requestId) {
                        SDKContext.getInstance().runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mAdLoadManager != null) {
                                    mAdLoadManager.sendRequestTimeoutAgent(requestId);
                                }

                                if (mListener != null) {
                                    mListener.onNoAdError(ErrorCode.getErrorCode(ErrorCode.timeOutError, "", ""));
                                }
                            }
                        });
                    }
                };

                mAdLoadListener.startCountDown(timeout);
                mAdLoadManager.startLoadAd(activity != null ? activity : mContext, mDefaultRequestInfo, mAdLoadListener, timeout);
            }
        });
    }

    public boolean isAdReady() {
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            Log.e(TAG, "SDK init error!");
            return false;
        }

        boolean isAdReady = mAdLoadManager.isAdReady(mContext);
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_SPLASH, Const.LOGKEY.API_ISREADY, String.valueOf(isAdReady), "");
        return isAdReady;
    }

    public ATAdStatusInfo checkAdStatus() {
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            Log.e(TAG, "SDK init error!");
            return new ATAdStatusInfo(false, false, null);
        }

        ATAdStatusInfo adStatusInfo = mAdLoadManager.checkAdStatus(mContext);
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_REWARD, Const.LOGKEY.API_AD_STATUS, adStatusInfo.toString(), "");

        return adStatusInfo;
    }

    public void show(Activity activity, final ViewGroup container) {
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_REWARD, Const.LOGKEY.API_SHOW, Const.LOGKEY.START, "");
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            Log.e(TAG, "SDK init error!");
            return;
        }

        if (activity == null) {
            Log.e(TAG, "Splash Activity is null.");
        }

        if (container == null) {
            Log.e(TAG, "Splash Container is null.");
            return;
        }


        AdEventListener adEventListener = new AdEventListener() {
            @Override
            public void onDeeplinkCallback(final ATAdInfo entity, final boolean isSuccess) {
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null && mListener instanceof ATSplashExListener) {
                            ((ATSplashExListener) mListener).onDeeplinkCallback(entity, isSuccess);
                        }
                    }
                });
            }

            @Override
            public void onDownloadConfirm(final Context context, final ATAdInfo adInfo, final ATNetworkConfirmInfo networkConfirmInfo) {
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null && mListener instanceof ATSplashExListenerWithConfirmInfo) {
                            ((ATSplashExListenerWithConfirmInfo) mListener).onDownloadConfirm(context == null ? mContext : context, adInfo, networkConfirmInfo);
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
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.onAdDismiss(entity);
                        }
                    }
                });
            }
        };

        mAdLoadManager.show(activity, container, adEventListener);
    }

    @Deprecated
    public void onDestory() {

    }

    public static void checkSplashDefaultConfigList(Context context, String splashPlacementId, Map<String, Object> customMap) {
        SDKContext.getInstance().checkSplashDefaultConfig(context, splashPlacementId, customMap);
    }
}
