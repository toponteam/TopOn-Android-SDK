/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.content.Context;
import android.view.View;

import com.anythink.basead.myoffer.manager.MyOfferAdManager;
import com.anythink.basead.listeners.AdListener;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.ui.BannerAdView;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;

import java.util.Map;

public class MyOfferBannerAd extends MyOfferBaseAd {
    private final String TAG = getClass().getSimpleName();

    AdListener mListener;

    public MyOfferBannerAd(Context context, String placementId, String offerId, MyOfferSetting myOfferSetting, boolean isDefault) {
        super(context, placementId, offerId, myOfferSetting, isDefault);
    }

    public void setListener(AdListener listener) {
        this.mListener = listener;
    }

    public View getBannerView(String requestId) {
        if (this.isReady()) {
            final BannerAdView bannerAdView = new BannerAdView(mContext, mPlacementId, requestId, mMyOfferAd, mMyOfferSetting, mListener);
            bannerAdView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bannerAdView.onClickBannerView();
                }
            });

            return bannerAdView;
        }
        return null;
    }

    @Override
    public void load() {
        try {
            OfferError myOfferError = checkLoadParams();
            if (myOfferError != null) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(myOfferError);
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


    @Override
    public void show(Map<String, Object> extraMap) {

    }

    @Override
    public boolean isReady() {
        try {
            if (checkIsReadyParams()) {
                return MyOfferAdManager.getInstance(mContext).isReady(mMyOfferAd, mMyOfferSetting, mIsDefault);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void destroy() {
        mListener = null;
    }
}
