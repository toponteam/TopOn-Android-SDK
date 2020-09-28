package com.anythink.myoffer.network.banner;

import android.content.Context;
import android.view.View;

import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.myoffer.buiness.MyOfferAdManager;
import com.anythink.myoffer.buiness.resource.MyOfferLoader;
import com.anythink.myoffer.network.base.MyOfferAdListener;
import com.anythink.myoffer.network.base.MyOfferBaseAd;
import com.anythink.myoffer.ui.BannerAdView;
import com.anythink.network.myoffer.MyOfferError;
import com.anythink.network.myoffer.MyOfferErrorCode;

import java.util.Map;

public class MyOfferBannerAd extends MyOfferBaseAd {
    private final String TAG = getClass().getSimpleName();

    MyOfferAdListener mListener;

    public MyOfferBannerAd(Context context, String placementId, String offerId, MyOfferSetting myOfferSetting, boolean isDefault) {
        super(context, placementId, offerId, myOfferSetting, isDefault);
    }

    public void setListener(MyOfferAdListener listener) {
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
            MyOfferError myOfferError = checkLoadParams();
            if (myOfferError != null) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(myOfferError);
                }
                return;
            }

            MyOfferAdManager.getInstance(mContext).load(mPlacementId, mMyOfferAd, mMyOfferSetting, new MyOfferLoader.MyOfferLoaderListener() {
                @Override
                public void onSuccess() {
                    if (mListener != null) {
                        mListener.onAdLoaded();
                    }
                }

                @Override
                public void onFailed(MyOfferError error) {
                    if (mListener != null) {
                        mListener.onAdLoadFailed(error);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onAdLoadFailed(MyOfferErrorCode.get(MyOfferErrorCode.unknow, e.getMessage()));
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
