/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.adx;

import android.content.Context;

import com.anythink.basead.adx.manager.AdxAdManager;
import com.anythink.basead.adx.utils.AdxOfferSettingUpdateUtil;
import com.anythink.basead.buiness.resource.OfferResourceState;
import com.anythink.basead.entity.AdActivityStartParams;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.listeners.AdEventMessager;
import com.anythink.basead.listeners.VideoAdListener;
import com.anythink.basead.myoffer.MyOfferBaseAd;
import com.anythink.basead.ui.BaseAdActivity;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.AdxRequestInfo;
import com.anythink.core.common.utils.CommonLogUtil;

import java.util.Map;


public class AdxInterstitialAd extends AdxBaseAd {

    public static final String TAG = AdxInterstitialAd.class.getSimpleName();

    private VideoAdListener mListener;
    private AdxAdConfig mAdxAdConfig;

    public AdxInterstitialAd(Context context, AdxRequestInfo adxRequestInfo) {
        super(context, adxRequestInfo);
    }

    public void setAdxAdConfig(AdxAdConfig adxAdConfig) {
        this.mAdxAdConfig = adxAdConfig;
        AdxOfferSettingUpdateUtil.update(mAdxRequestInfo.adxAdSetting, mAdxAdConfig);
    }

    public void setListener(VideoAdListener listener) {
        this.mListener = listener;
    }

    @Override
    public void load() {

        try {
            OfferError offerError = checkLoadParams();
            if (offerError != null) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(offerError);
                }
                return;
            }

            AdxAdManager.getInstance(mContext).loadAd(mAdxRequestInfo, new AdxAdManager.AdxAdLoadListener() {
                @Override
                public void onAdDataLoaded(AdxOffer adxOffer) {
                    if (mListener != null) {
                        mListener.onAdDataLoaded();
                    }
                }

                @Override
                public void onAdCacheLoaded(AdxOffer adxOffer) {
                    mAdxOffer = adxOffer;
                    if (mListener != null) {
                        mListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onAdError(OfferError offerError) {
                    if (mListener != null) {
                        mListener.onAdLoadFailed(offerError);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onAdLoadFailed(OfferErrorCode.get(OfferErrorCode.unknow, e.getMessage()));
            }
        }

    }


    public void show(Map<String, Object> extra) {

        try {
            OfferError offerError = checkShowParams();
            if (offerError != null) {
                if (mListener != null) {
                    mListener.onVideoShowFailed(offerError);
                }
                mAdxOffer = null;
                return;
            }

            final String scenario = extra.get(MyOfferBaseAd.EXTRA_SCENARIO).toString();
            final int orientation = (int) extra.get(MyOfferBaseAd.EXTRA_ORIENTATION);
            final String eventId = mAdxRequestInfo.placementId + mAdxOffer.getOfferId() + System.currentTimeMillis();

            AdEventMessager.getInstance().setListener(eventId, new AdEventMessager.OnEventListener() {
                @Override
                public void onShow() {
                    if (mListener != null) {
                        mListener.onAdShow();
                    }
                    mAdxOffer = null;
                }

                @Override
                public void onVideoShowFailed(OfferError error) {
                    if (mListener != null) {
                        mListener.onVideoShowFailed(error);
                    }
                    mAdxOffer = null;
                }

                @Override
                public void onVideoPlayStart() {
                    if (mListener != null) {
                        mListener.onVideoAdPlayStart();
                    }
                }

                @Override
                public void onVideoPlayEnd() {
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
            adActivityStartParams.baseAdContent = mAdxOffer;
            adActivityStartParams.eventId = eventId;
            adActivityStartParams.format = BaseAdActivity.FORMAT_INTERSTITIAL;
            adActivityStartParams.baseAdSetting = mAdxRequestInfo.adxAdSetting;
            adActivityStartParams.orientation = orientation;
            adActivityStartParams.placementId = mAdxRequestInfo.placementId;
            adActivityStartParams.requestId = mAdxRequestInfo.requestId;
            adActivityStartParams.scenario = scenario;

            BaseAdActivity.start(mContext, adActivityStartParams);
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onVideoShowFailed(OfferErrorCode.get(OfferErrorCode.unknow, e.getMessage()));
            }
            mAdxOffer = null;
        }

    }

    public boolean isReady() {
        if (mAdxOffer == null) {
            mAdxOffer = AdxAdManager.getInstance(mContext).getAdxOfferFromDiskCache(mAdxRequestInfo);
        }

        if (mAdxOffer != null && OfferResourceState.isExist(mAdxOffer, mAdxRequestInfo.adxAdSetting)) {
            return true;
        }
        return false;
    }

    @Override
    public void destory() {
        mAdxOffer = null;
        mListener = null;
    }

}
