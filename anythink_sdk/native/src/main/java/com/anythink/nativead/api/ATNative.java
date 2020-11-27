/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.api;

import android.content.Context;

import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.common.PlacementAdManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdCacheInfo;
import com.anythink.nativead.bussiness.AdLoadManager;

import java.util.Map;

/**
 * Created by Z on 2017/12/28.
 */

public class ATNative {
    Context mContext;
    String mPlacementId;
    ATNativeNetworkListener mListener;

    Map<String, Object> mLocalMap;

    AdLoadManager mAdLoadManager;

    ATNativeOpenSetting mOpenSetting = new ATNativeOpenSetting();

    @Deprecated
    public static final String KEY_WIDTH = "key_width";
    @Deprecated
    public static final String KEY_HEIGHT = "key_height";


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


    @Deprecated
    public void makeAdRequest(Map<String, String> customMap) {

        ATSDK.apiLog(mPlacementId, Const.LOGKEY.API_NATIVE, Const.LOGKEY.API_LOAD, Const.LOGKEY.START, "");

        mAdLoadManager.startLoadAd(mContext, mSelfListener);
    }

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

        AdCacheInfo adCacheInfo = mAdLoadManager.showNativeAd();
        if (adCacheInfo != null) {
            NativeAd nativeAd = new NativeAd(mContext, mPlacementId, adCacheInfo);
            return nativeAd;
        }
        return null;
    }


    public ATNativeOpenSetting getOpenSetting() {
        if (mAdLoadManager != null) {
            mAdLoadManager.setOpenSetting(mOpenSetting, mPlacementId);
        }
        return mOpenSetting;
    }

}
