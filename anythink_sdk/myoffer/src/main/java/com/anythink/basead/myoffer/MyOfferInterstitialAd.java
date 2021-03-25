/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.content.Context;

import com.anythink.basead.entity.AdActivityStartParams;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.listeners.AdEventMessager;
import com.anythink.basead.listeners.VideoAdEventListener;
import com.anythink.basead.ui.BaseAdActivity;
import com.anythink.basead.ui.FullScreenAdView;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.utils.CommonLogUtil;

import java.util.Map;

public class MyOfferInterstitialAd extends MyOfferBaseAd {

    public static final String TAG = MyOfferInterstitialAd.class.getSimpleName();

    private VideoAdEventListener mListener;

    public MyOfferInterstitialAd(Context context, BaseAdRequestInfo requestInfo, String offerId, boolean isDefault) {
        super(context, requestInfo, offerId, isDefault);
    }

    public void setListener(VideoAdEventListener listener) {
        this.mListener = listener;
    }


    @Override
    public void show(Map<String, Object> extraMap) {

        try {
            if (mContext == null) {
                if (mListener != null) {
                    mListener.onVideoShowFailed(OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_null_context));
                }
                return;
            }

            final String requestId = extraMap.get(MyOfferBaseAd.EXTRA_REQUEST_ID).toString();
            final String scenario = extraMap.get(MyOfferBaseAd.EXTRA_SCENARIO).toString();
            final int orientation = (int) extraMap.get(MyOfferBaseAd.EXTRA_ORIENTATION);
            final String eventId = mRequestInfo.placementId + mOfferId + System.currentTimeMillis();
            AdEventMessager.getInstance().setListener(eventId, new AdEventMessager.OnEventListener() {
                @Override
                public void onShow() {
                    CommonLogUtil.d(TAG, "onShow.......");
                    if (mListener != null) {
                        mListener.onAdShow();
                    }
                }

                @Override
                public void onVideoShowFailed(OfferError error) {
                    CommonLogUtil.d(TAG, "onVideoShowFailed......." + error.printStackTrace());
                    if (mListener != null) {
                        mListener.onVideoShowFailed(error);
                    }
                }

                @Override
                public void onVideoPlayStart() {
                    CommonLogUtil.d(TAG, "onVideoPlayStart.......");
                    if (mListener != null) {
                        mListener.onVideoAdPlayStart();
                    }
                }

                @Override
                public void onVideoPlayEnd() {
                    CommonLogUtil.d(TAG, "onVideoPlayEnd.......");
                    if (mListener != null) {
                        mListener.onVideoAdPlayEnd();
                    }
                }

                @Override
                public void onReward() {
                }

                @Override
                public void onClose() {
                    CommonLogUtil.d(TAG, "onClose.......");
                    if (mListener != null) {
                        mListener.onAdClosed();
                    }
                    AdEventMessager.getInstance().unRegister(eventId);
                }

                @Override
                public void onClick() {
                    CommonLogUtil.d(TAG, "onClick.......");
                    if (mListener != null) {
                        mListener.onAdClick();
                    }
                }

                @Override
                public void onDeeplinkCallback(boolean isSuccess) {
                    CommonLogUtil.d(TAG, "onDeeplinkCallback.......:" + isSuccess);
                    if (mListener != null) {
                        mListener.onDeeplinkCallback(isSuccess);
                    }
                }

            });

            AdActivityStartParams adActivityStartParams = new AdActivityStartParams();
            adActivityStartParams.baseAdContent = mMyOfferAd;
            adActivityStartParams.eventId = eventId;
            adActivityStartParams.format = FullScreenAdView.FORMAT_INTERSTITIAL;
            adActivityStartParams.baseAdRequestInfo = mRequestInfo;
            adActivityStartParams.orientation = orientation;
            adActivityStartParams.scenario = scenario;

            BaseAdActivity.start(mContext, adActivityStartParams);
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onVideoShowFailed(OfferErrorCode.get(OfferErrorCode.unknow, e.getMessage()));
            }
        }

    }


}
