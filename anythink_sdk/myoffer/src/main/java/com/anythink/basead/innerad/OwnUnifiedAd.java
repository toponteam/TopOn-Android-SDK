/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.OfferClickController;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.impression.ImpressionController;
import com.anythink.basead.impression.ImpressionTracker;
import com.anythink.basead.innerad.onlineapi.OnlineApiAdCacheManager;
import com.anythink.basead.innerad.utils.OwnOfferImpressionRecordManager;
import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.ui.OwnNativeAdView;
import com.anythink.core.common.adx.AdxCacheController;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.OnlineApiOffer;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.hb.BiddingCacheManager;

import java.util.List;

public class OwnUnifiedAd {

    Context mContext;

    AdEventListener mListener;

    ImpressionTracker mImpressionTracker;

    OfferClickController mOfferClickControl;

    View mAdView;

    boolean hadRecordImpression;

    OwnBaseAdContent mOwnBaseAdContent;
    BaseAdRequestInfo mRequestInfo;

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOwnNativeAdView != null) {
                Context context = v.getContext().getApplicationContext();
                if (mOfferClickControl == null) {
                    mOfferClickControl = new OfferClickController(context, mRequestInfo, mOwnBaseAdContent);
                }

                if (mListener != null) {
                    mListener.onAdClick();
                }

                UserOperateRecord userOperateRecord = new UserOperateRecord(mRequestInfo.requestId, "");
                userOperateRecord.realHeight = mOwnNativeAdView.getHeight();
                userOperateRecord.realWidth = mOwnNativeAdView.getWidth();
                userOperateRecord.adClickRecord = mOwnNativeAdView.getAdClickRecord();

                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.CLICK_TYPE, mOwnBaseAdContent, userOperateRecord);
                mOfferClickControl.startClick(userOperateRecord, new OfferClickController.ClickStatusCallback() {
                    @Override
                    public void clickStart() {
                    }

                    @Override
                    public void clickEnd() {
                    }

                    @Override
                    public void deeplinkCallback(boolean isSuccess) {
                        if (mListener != null) {
                            mListener.onDeeplinkCallback(isSuccess);
                        }
                    }
                });
            }
        }
    };


    public OwnUnifiedAd(Context context, OwnBaseAdContent ownBaseAdContent, BaseAdRequestInfo ownBaseAdRequestInfo) {
        mContext = context.getApplicationContext();
        this.mOwnBaseAdContent = ownBaseAdContent;
        this.mRequestInfo = ownBaseAdRequestInfo;

    }

    public BaseAdContent getBaseAdContent() {
        return mOwnBaseAdContent;
    }

    public String getTitle() {
        if (mOwnBaseAdContent != null) {
            return mOwnBaseAdContent.getTitle();
        }
        return "";
    }

    public String getDesctiption() {
        if (mOwnBaseAdContent != null) {
            return mOwnBaseAdContent.getDesc();
        }
        return "";
    }

    public String getCallToAction() {
        if (mOwnBaseAdContent != null) {
            return mOwnBaseAdContent.getCtaText();
        }
        return "";
    }

    public String getIcon() {
        if (mOwnBaseAdContent != null) {
            return mOwnBaseAdContent.getIconUrl();
        }
        return "";
    }

    public String getMainImageUrl() {
        if (mOwnBaseAdContent != null) {
            return mOwnBaseAdContent.getEndCardImageUrl();
        }
        return "";
    }

    public String getAdChoiceIconUrl() {
        if (mOwnBaseAdContent != null) {
            return mOwnBaseAdContent.getAdChoiceUrl();
        }
        return "";
    }


    public View getMediaView(Context context) {
//        if (mAdxOffer != null) {
//            if (AdxOffer.CREATIVE_TYPE_VIDEO == Integer.parseInt(mAdxOffer.getCreativeType())) {
//                //todo sava process
//                FrameLayout container = new FrameLayout(context);
//                PlayerView playerView = new PlayerView(container, new PlayerView.OnPlayerListener() {
//                    @Override
//                    public void onVideoPlayStart() {
//
//                    }
//
//                    @Override
//                    public void onVideoUpdateProgress(int progress) {
//
//                    }
//
//                    @Override
//                    public void onVideoPlayEnd() {
//
//                    }
//
//                    @Override
//                    public void onVideoPlayCompletion() {
//
//                    }
//
//                    @Override
//                    public void onVideoShowFailed(OfferError error) {
//
//                    }
//
//                    @Override
//                    public void onVideoPlayProgress(int progressArea) {
//
//                    }
//
//                    @Override
//                    public void onVideoCloseClick() {
//
//                    }
//
//                    @Override
//                    public void onVideoClick() {
//
//                    }
//
//                    @Override
//                    public void onVideoMute() {
//
//                    }
//
//                    @Override
//                    public void onVideoNoMute() {
//
//                    }
//                });
//                playerView.setSetting(mAdxRequestInfo.adxAdSetting);
//                playerView.load(mAdxOffer.getVideoUrl());
//
//                return playerView;
//            }
//        }


        return null;
    }


    public void setListener(AdEventListener listener) {
        this.mListener = listener;
    }


    public void registerAdView(View view, List<View> clickViews) {
        if (!checkRegisterViewExistOwnNativeAdView(view)) {
            return;
        }
        resigterImpressionView(view);
        if (clickViews != null) {
            for (View childView : clickViews) {
                childView.setOnClickListener(clickListener);
            }
        } else {
            view.setOnClickListener(clickListener);
        }
    }

    public void registerAdView(View view) {
        if (!checkRegisterViewExistOwnNativeAdView(view)) {
            return;
        }
        resigterImpressionView(view);
//        view.setOnClickListener(clickListener);
        loopToRegisterChildView(view, clickListener);
    }


    OwnNativeAdView mOwnNativeAdView;

    private boolean checkRegisterViewExistOwnNativeAdView(View view) {
        OwnNativeAdView[] ownNativeAdViews = new OwnNativeAdView[1];
        getOwnNativeAdView(ownNativeAdViews, view);
        if (ownNativeAdViews[0] == null) {
            Log.i(Const.RESOURCE_HEAD, "Register View don't contain OwnNativeAdView.");
            return false;
        }

        if (ownNativeAdViews[0].getChildCount() == 0) {
            Log.i(Const.RESOURCE_HEAD, "OwnNativeAdView View don't contain any child views.");
            return false;
        }

        mOwnNativeAdView = ownNativeAdViews[0];

        return true;
    }

    private void getOwnNativeAdView(OwnNativeAdView[] ownNativeAdViews, View view) {
        if (view instanceof ViewGroup) {
            if (view instanceof OwnNativeAdView) {
                ownNativeAdViews[0] = (OwnNativeAdView) view;
            }
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                getOwnNativeAdView(ownNativeAdViews, child);
            }
        }
    }

    private void loopToRegisterChildView(View view, View.OnClickListener clickListener) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                loopToRegisterChildView(child, clickListener);
            }
        } else {
            view.setOnClickListener(clickListener);
        }
    }

    public void unregisterView() {
        if (mImpressionTracker != null) {
            mImpressionTracker.clear();
        }
    }

    private void resigterImpressionView(View view) {
        mAdView = view;
        ImpressionController nativeImpressController = new ImpressionController() {
            @Override
            public void recordImpression(View view) {
                notifyShow();
            }
        };

        if (mImpressionTracker == null) {
            mImpressionTracker = new ImpressionTracker(view.getContext());
        }

        if (mOwnBaseAdContent instanceof OnlineApiOffer) {
            OnlineApiAdCacheManager.getInstance().removeOnlineApiOfferInfo(mContext, OnlineApiAdCacheManager.getInstance().getOnlineApiSaveId(mRequestInfo));
        }

        if (mOwnBaseAdContent instanceof AdxOffer) {
            BiddingCacheManager.getInstance().removeCache(mRequestInfo.adsourceId, Const.NETWORK_FIRM.ADX_NETWORK_FIRM_ID);
            AdxCacheController.getInstance().removeAdxOfferInfo(mContext, ((AdxOffer) mOwnBaseAdContent).getBidId());
        }

        mImpressionTracker.addView(view, nativeImpressController);


    }

    private void notifyShow() {
        if (hadRecordImpression) {
            return;
        }

        hadRecordImpression = true;
        if (mOwnBaseAdContent instanceof OnlineApiOffer) {
            OwnOfferImpressionRecordManager.getInstance().recordOfferImpression(mContext, OwnOfferImpressionRecordManager.getRecordId(mRequestInfo.placementId, mRequestInfo.adsourceId), mOwnBaseAdContent, mRequestInfo.baseAdSetting);
        }

        if (mOwnNativeAdView != null) {
            UserOperateRecord userOperateRecord = new UserOperateRecord(mRequestInfo.requestId, "");
            userOperateRecord.realHeight = mOwnNativeAdView.getHeight();
            userOperateRecord.realWidth = mOwnNativeAdView.getWidth();
            OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.IMPRESSION_TYPE, mOwnBaseAdContent, userOperateRecord);
            if (mListener != null) {
                mListener.onAdShow();
            }
        }

    }

    public void destroy() {
        unregisterView();
        mAdView = null;
        mOwnNativeAdView = null;
        mListener = null;
        mOfferClickControl = null;
        if (mImpressionTracker != null) {
            mImpressionTracker.destroy();
            mImpressionTracker = null;
        }

    }

}
