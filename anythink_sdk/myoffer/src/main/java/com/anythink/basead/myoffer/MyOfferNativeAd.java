/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.OfferClickController;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.impression.ImpressionController;
import com.anythink.basead.impression.ImpressionTracker;
import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.myoffer.manager.MyOfferImpressionRecordManager;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.entity.MyOfferSetting;

import java.util.List;
import java.util.Map;

public class MyOfferNativeAd extends MyOfferBaseAd {

    private final String TAG = getClass().getSimpleName();

    AdEventListener mListener;

    ImpressionTracker mImpressionTracker;

    OfferClickController mOfferClickControl;


    View mAdView;

    boolean hadRecordImpression;

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOfferClickControl == null) {
                mOfferClickControl = new OfferClickController(mContext, mRequestInfo, mMyOfferAd);
            }

            if (mListener != null) {
                mListener.onAdClick();
            }

            OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.CLICK_TYPE, mMyOfferAd, new UserOperateRecord(mRequestInfo.requestId, ""));
            mOfferClickControl.startClick(new UserOperateRecord(mRequestInfo.requestId, ""), new OfferClickController.ClickStatusCallback() {
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
    };


//    View.OnAttachStateChangeListener onAttachStateChangeListene = new View.OnAttachStateChangeListener() {
//        @Override
//        public void onViewAttachedToWindow(View v) {
//            if (v.getVisibility() == View.VISIBLE) {
//                notifyShow();
//            }
//        }
//
//        @Override
//        public void onViewDetachedFromWindow(View v) {
//
//        }
//    };

    public MyOfferNativeAd(Context context, BaseAdRequestInfo baseAdRequestInfo, String offerId, boolean isDefault) {
        super(context, baseAdRequestInfo, offerId, isDefault);
    }


    public String getTitle() {
        if (mMyOfferAd != null) {
            return mMyOfferAd.getTitle();
        }
        return "";
    }

    public String getDesctiption() {
        if (mMyOfferAd != null) {
            return mMyOfferAd.getDesc();
        }
        return "";
    }

    public String getCallToAction() {
        if (mMyOfferAd != null) {
            return mMyOfferAd.getCtaText();
        }
        return "";
    }

    public String getIcon() {
        if (mMyOfferAd != null) {
            return mMyOfferAd.getIconUrl();
        }
        return "";
    }

    public String getMainImageUrl() {
        if (mMyOfferAd != null) {
            return mMyOfferAd.getEndCardImageUrl();
        }
        return "";
    }

    public String getAdChoiceIconUrl() {
        if (mMyOfferAd != null) {
            return mMyOfferAd.getAdChoiceUrl();
        }
        return "";
    }

    public View getMediaView() {
        return null;
    }


    public void setListener(AdEventListener listener) {
        this.mListener = listener;
    }

    @Override
    public void show(Map<String, Object> extraMap) {

    }

    public void registerAdView(View view, List<View> clickViews) {
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
        resigterImpressionView(view);
//        view.setOnClickListener(clickListener);
        loopToRegisterChildView(view, clickListener);
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

        mImpressionTracker.addView(view, nativeImpressController);

    }

    private void notifyShow() {
        if (hadRecordImpression) {
            return;
        }

        hadRecordImpression = true;
        MyOfferImpressionRecordManager.getInstance(mContext).recordImpression(mMyOfferAd);
        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.IMPRESSION_TYPE, mMyOfferAd, new UserOperateRecord(mRequestInfo.requestId, ""));

        if (mListener != null) {
            mListener.onAdShow();
        }
    }

    public void destory() {
        unregisterView();
        mListener = null;
        mOfferClickControl = null;
        mImpressionTracker = null;
    }
}
