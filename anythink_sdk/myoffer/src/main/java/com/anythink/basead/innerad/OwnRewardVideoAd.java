/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad;

import android.content.Context;

import com.anythink.basead.entity.AdActivityStartParams;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.listeners.AdEventMessager;
import com.anythink.basead.listeners.VideoAdEventListener;
import com.anythink.basead.myoffer.MyOfferBaseAd;
import com.anythink.basead.ui.BaseAdActivity;
import com.anythink.basead.ui.FullScreenAdView;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.utils.CommonLogUtil;

import java.util.Map;


public class OwnRewardVideoAd extends OwnBaseAd {

    public static final String TAG = OwnRewardVideoAd.class.getSimpleName();

    private VideoAdEventListener mListener;

    public OwnRewardVideoAd(Context context, OFFER_TYPE offerType, BaseAdRequestInfo ownBaseAdRequestInfo) {
        super(context, offerType, ownBaseAdRequestInfo);
    }


    public void setListener(VideoAdEventListener listener) {
        this.mListener = listener;
    }


    public void show(Map<String, Object> extra) {

        try {
            if (!isAdReady()) {
                if (mListener != null) {
                    mListener.onVideoShowFailed(OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_no_offer));
                }
                mBaseAdContent = null;
                return;
            }

            final String scenario = extra.get(MyOfferBaseAd.EXTRA_SCENARIO).toString();
            final int orientation = (int) extra.get(MyOfferBaseAd.EXTRA_ORIENTATION);
            final String eventId = getAdEventId(mBaseAdContent);

            AdEventMessager.getInstance().setListener(eventId, new AdEventMessager.OnEventListener() {
                @Override
                public void onShow() {
                    if (mListener != null) {
                        mListener.onAdShow();
                    }
                    mBaseAdContent = null;
                }

                @Override
                public void onVideoShowFailed(OfferError error) {
                    if (mListener != null) {
                        mListener.onVideoShowFailed(error);
                    }
                    mBaseAdContent = null;
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
                    if (mListener != null) {
                        mListener.onRewarded();
                    }
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
            adActivityStartParams.baseAdContent = mBaseAdContent;
            adActivityStartParams.eventId = eventId;
            adActivityStartParams.format = FullScreenAdView.FORMAT_REWARD_VIDEO;
            adActivityStartParams.baseAdRequestInfo = mOwnBaseAdRequestInfo;
            adActivityStartParams.orientation = orientation;
            adActivityStartParams.scenario = scenario;

            BaseAdActivity.start(mContext, adActivityStartParams);
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onVideoShowFailed(OfferErrorCode.get(OfferErrorCode.unknow, e.getMessage()));
            }
            mBaseAdContent = null;
        }

    }


    @Override
    public void destroy() {
        super.destroy();
        mListener = null;
    }

}
