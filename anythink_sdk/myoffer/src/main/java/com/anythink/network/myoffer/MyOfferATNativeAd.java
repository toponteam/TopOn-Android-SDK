package com.anythink.network.myoffer;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.anythink.myoffer.network.base.MyOfferAdListener;
import com.anythink.myoffer.network.nativead.MyOfferNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;

import java.util.List;

public class MyOfferATNativeAd extends CustomNativeAd {
    MyOfferNativeAd mMyOfferNativeAd;
    Context mContext;

    public MyOfferATNativeAd(Context context, MyOfferNativeAd myOfferNativeAd) {
        mContext = context.getApplicationContext();
        mMyOfferNativeAd = myOfferNativeAd;
        mMyOfferNativeAd.setListener(new MyOfferAdListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdLoadFailed(MyOfferError error) {

            }

            @Override
            public void onAdShow() {

            }

            @Override
            public void onAdClosed() {

            }

            @Override
            public void onAdClick() {
                notifyAdClicked();
            }
        });

        setAdChoiceIconUrl(mMyOfferNativeAd.getAdChoiceIconUrl());
        setTitle(mMyOfferNativeAd.getTitle());
        setDescriptionText(mMyOfferNativeAd.getDesctiption());
        setIconImageUrl(mMyOfferNativeAd.getIcon());
        setMainImageUrl(mMyOfferNativeAd.getMainImageUrl());
        setCallToActionText(mMyOfferNativeAd.getCallToAction());
    }

    @Override
    public View getAdMediaView(Object... object) {
        return mMyOfferNativeAd.getMediaView();
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {
        if (mMyOfferNativeAd != null) {
            mMyOfferNativeAd.registerAdView(getDetail().getmRequestId(), view, clickViewList);
        }
    }

    @Override
    public void prepare(View view, FrameLayout.LayoutParams layoutParams) {
        if (mMyOfferNativeAd != null) {
            mMyOfferNativeAd.registerAdView(getDetail().getmRequestId(), view);
        }
    }

    @Override
    public void clear(View view) {
        if (mMyOfferNativeAd != null) {
            mMyOfferNativeAd.unregisterView();
        }
    }

    @Override
    public void destroy() {
        if (mMyOfferNativeAd != null) {
            mMyOfferNativeAd.setListener(null);
            mMyOfferNativeAd.destory();
        }
    }
}
