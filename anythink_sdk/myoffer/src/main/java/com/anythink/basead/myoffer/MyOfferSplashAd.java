/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.anythink.basead.myoffer.manager.MyOfferAdManager;
import com.anythink.basead.listeners.AdListener;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.ui.SplashAdView;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;

import java.util.Map;

public class MyOfferSplashAd extends MyOfferBaseAd {

    AdListener mListener;
    MyOfferAd mMyOfferAd;

    String mRequestId;

    public MyOfferSplashAd(Context context, String placementId, String offerId, MyOfferSetting myoffer_setting, String requestId, boolean isDefault) {
        super(context, placementId, offerId, myoffer_setting, isDefault);
        mRequestId = requestId;
    }


    @Override
    public void load() {
        try {
            if (TextUtils.isEmpty(mOfferId) || TextUtils.isEmpty(mPlacementId)) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_params));
                }
                return;
            }
            mMyOfferAd = MyOfferAdManager.getInstance(mContext).getAdCache(mPlacementId, mOfferId);
            if (mMyOfferAd == null) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_no_offer));
                }
                return;
            }
            if (mMyOfferSetting == null) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(OfferErrorCode.get(OfferErrorCode.noSettingError, OfferErrorCode.fail_no_setting));
                }
                return;
            }

            MyOfferAdManager.getInstance(mContext).load(mPlacementId, mMyOfferAd, mMyOfferSetting, new OfferResourceLoader.ResourceLoaderListener() {
                @Override
                public void onSuccess() {
                    if (mListener != null) {
                        mListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onFailed(OfferError error) {
                    if (mListener != null) {
                        mListener.onAdLoadFailed(error);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onAdLoadFailed(OfferErrorCode.get(OfferErrorCode.unknow, e.getMessage()));
            }
        }

    }

    /**
     * Only for Splash
     *
     * @param container
     */
    public void show(final ViewGroup container) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                container.addView(new SplashAdView(container.getContext(), mPlacementId, mRequestId, mMyOfferAd, mMyOfferSetting, mListener));
            }
        });

    }

    public void setListener(AdListener listener) {
        this.mListener = listener;
    }

    @Override
    public void show(Map<String, Object> extraMap) {

    }

    @Override
    public boolean isReady() {
        return false;
    }


    public void destory() {
        mListener = null;
    }
}
