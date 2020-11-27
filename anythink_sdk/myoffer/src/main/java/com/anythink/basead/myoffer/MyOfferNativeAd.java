/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.OfferClickController;
import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.impression.ImpressionController;
import com.anythink.basead.impression.ImpressionTracker;
import com.anythink.basead.listeners.AdListener;
import com.anythink.basead.myoffer.manager.MyOfferAdManager;
import com.anythink.basead.myoffer.manager.MyOfferImpressionRecordManager;
import com.anythink.core.common.entity.MyOfferSetting;

import java.util.List;
import java.util.Map;

public class MyOfferNativeAd extends MyOfferBaseAd {

    private final String TAG = getClass().getSimpleName();

    AdListener mListener;

    ImpressionTracker mImpressionTracker;

    OfferClickController mOfferClickControl;

    String mRequestId;

    View mAdView;

    boolean hadRecordImpression;

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOfferClickControl == null) {
                mOfferClickControl = new OfferClickController(mContext, mPlacementId, mMyOfferAd
                        , mMyOfferSetting);
            }

            if (mListener != null) {
                mListener.onAdClick();
            }

            OfferAdFunctionUtil.sendAdTracking(mRequestId, mMyOfferAd, OfferAdFunctionUtil.CLICK_TYPE, "");
            mOfferClickControl.startClick(mRequestId, new OfferClickController.ClickStatusCallback() {
                @Override
                public void clickStart() {
                }

                @Override
                public void clickEnd() {
                }

                @Override
                public void downloadApp(final String url) {
                    OfferAdFunctionUtil.startDownloadApp(mContext, mRequestId, mMyOfferSetting, mMyOfferAd, url);
                }
            });
        }
    };


    View.OnAttachStateChangeListener onAttachStateChangeListene = new View.OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(View v) {
            if (v.getVisibility() == View.VISIBLE) {
                notifyShow();
            }
        }

        @Override
        public void onViewDetachedFromWindow(View v) {

        }
    };

    public MyOfferNativeAd(Context context, String placementId, String offerId, MyOfferSetting myoffer_setting, boolean isDefault) {
        super(context, placementId, offerId, myoffer_setting, isDefault);
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


    public void setListener(AdListener listener) {
        this.mListener = listener;
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


    public void registerAdView(String requestId, View view, List<View> clickViews) {
        resigterImpressionView(requestId, view);
        if (clickViews != null) {
            for (View childView : clickViews) {
                childView.setOnClickListener(clickListener);
            }
        } else {
            view.setOnClickListener(clickListener);
        }
    }

    public void registerAdView(String requestId, View view) {
        resigterImpressionView(requestId, view);
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

        if (mAdView != null) {
            mAdView.removeOnAttachStateChangeListener(onAttachStateChangeListene);
            mAdView = null;
        }
    }

    private void resigterImpressionView(String requestId, View view) {
        mRequestId = requestId;
        mAdView = view;
        Context context = view.getContext();
        if (context instanceof Activity) {
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

        } else {
            view.addOnAttachStateChangeListener(onAttachStateChangeListene);
        }

    }

    private void notifyShow() {
        if (hadRecordImpression) {
            return;
        }

        hadRecordImpression = true;
        MyOfferImpressionRecordManager.getInstance(mContext).recordImpression(mMyOfferAd);
        OfferAdFunctionUtil.sendAdTracking(mRequestId, mMyOfferAd, OfferAdFunctionUtil.IMPRESSION_TYPE, "");

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
