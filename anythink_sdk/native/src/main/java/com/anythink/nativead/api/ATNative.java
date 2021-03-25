/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATAdStatusInfo;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.nativead.bussiness.AdLoadManager;

import java.util.Map;


public class ATNative {
    private final String TAG = ATNative.class.getSimpleName();
    Context mContext;
    String mPlacementId;
    ATNativeNetworkListener mListener;


    AdLoadManager mAdLoadManager;

    ATNativeOpenSetting mOpenSetting = new ATNativeOpenSetting();

    /**
     * Init
     *
     * @param context
     * @param placementId
     * @param listener
     */
    public ATNative(Context context
            , String placementId
            , ATNativeNetworkListener listener) {
        mContext = context;
        mPlacementId = placementId;
        mListener = listener;

        mAdLoadManager = AdLoadManager.getInstance(context, placementId);
    }


    ATNativeNetworkListener mSelfListener = new ATNativeNetworkListener() {
        @Override
        public void onNativeAdLoaded() {
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onNativeAdLoaded();
                    }
                }
            });

        }

        @Override
        public void onNativeAdLoadFail(final AdError error) {
            if (mAdLoadManager != null) {
                mAdLoadManager.setLoadFail(error);
            }
            SDKContext.getInstance().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        mListener.onNativeAdLoadFail(error);
                    }
                }
            });
        }
    };

    /**
     * Ad Request
     */
    public void makeAdRequest() {
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_NATIVE, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");

        mAdLoadManager.startLoadAd(mContext, mSelfListener);
    }

    /**
     * Mediation Setting Map
     *
     * @param map
     */
    public void setLocalExtra(Map<String, Object> map) {
        PlacementAdManager.getInstance().putPlacementLocalSettingMap(mPlacementId, map);
    }

    /**
     * Get Native Ad
     *
     * @return
     */
    public NativeAd getNativeAd() {

        AdCacheInfo adCacheInfo = mAdLoadManager.showNativeAd("");
        if (adCacheInfo != null) {
            NativeAd nativeAd = new NativeAd(mContext, mPlacementId, adCacheInfo);
            return nativeAd;
        }
        return null;
    }

    public NativeAd getNativeAd(String scenario) {
        String realScenario = "";
        if (CommonSDKUtil.isVailScenario(scenario)) {
            realScenario = scenario;
        }
        AdCacheInfo adCacheInfo = mAdLoadManager.showNativeAd(realScenario);
        if (adCacheInfo != null) {
            NativeAd nativeAd = new NativeAd(mContext, mPlacementId, adCacheInfo);
            return nativeAd;
        }
        return null;
    }

    public ATAdStatusInfo checkAdStatus() {
        if (SDKContext.getInstance().getContext() == null
                || TextUtils.isEmpty(SDKContext.getInstance().getAppId())
                || TextUtils.isEmpty(SDKContext.getInstance().getAppKey())) {
            Log.e(TAG, "SDK init error!");
            return new ATAdStatusInfo(false, false, null);
        }

        ATAdStatusInfo adStatusInfo = mAdLoadManager.checkAdStatus(mContext);
        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_NATIVE, Const.LOGKEY.API_AD_STATUS, adStatusInfo.toString(), "");

        return adStatusInfo;
    }


    public ATNativeOpenSetting getOpenSetting() {
        if (mAdLoadManager != null) {
            mAdLoadManager.setOpenSetting(mOpenSetting, mPlacementId);
        }
        return mOpenSetting;
    }

}
