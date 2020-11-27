/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.content.Context;

import com.anythink.basead.entity.AdActivityStartParams;
import com.anythink.basead.myoffer.manager.MyOfferAdManager;
import com.anythink.basead.listeners.AdEventMessager;
import com.anythink.basead.listeners.VideoAdListener;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.basead.myoffer.manager.MyOfferImpressionRecordManager;
import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.ui.BaseAdActivity;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;

import java.util.Map;

public class MyOfferInterstitialAd extends MyOfferBaseAd {

    public static final String TAG = MyOfferInterstitialAd.class.getSimpleName();

    private VideoAdListener mListener;

    public MyOfferInterstitialAd(Context context, String placementId, String offerId, MyOfferSetting myoffer_setting, boolean isDefault) {
        super(context, placementId, offerId, myoffer_setting, isDefault);
    }

    public void setListener(VideoAdListener listener) {
        this.mListener = listener;
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
            final String eventId = mPlacementId + mOfferId + System.currentTimeMillis();
            AdEventMessager.getInstance().setListener(eventId, new AdEventMessager.OnEventListener() {
                @Override
                public void onShow() {
                    CommonLogUtil.d(TAG, "onShow.......");
                    if (mListener != null) {
                        mListener.onAdShow();
                    }
                    MyOfferImpressionRecordManager.getInstance(mContext).recordImpression(mMyOfferAd);
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

            });

            AdActivityStartParams adActivityStartParams = new AdActivityStartParams();
            adActivityStartParams.baseAdContent = mMyOfferAd;
            adActivityStartParams.eventId = eventId;
            adActivityStartParams.format = BaseAdActivity.FORMAT_INTERSTITIAL;
            adActivityStartParams.baseAdSetting = mMyOfferSetting;
            adActivityStartParams.orientation = orientation;
            adActivityStartParams.placementId = mPlacementId;
            adActivityStartParams.requestId = requestId;
            adActivityStartParams.scenario = scenario;

            BaseAdActivity.start(mContext, adActivityStartParams);
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onVideoShowFailed(OfferErrorCode.get(OfferErrorCode.unknow, e.getMessage()));
            }
        }

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


}
