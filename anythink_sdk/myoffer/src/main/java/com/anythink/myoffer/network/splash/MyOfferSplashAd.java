package com.anythink.myoffer.network.splash;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.myoffer.buiness.MyOfferAdManager;
import com.anythink.myoffer.buiness.OfferClickController;
import com.anythink.myoffer.buiness.resource.MyOfferLoader;
import com.anythink.myoffer.network.base.MyOfferAdListener;
import com.anythink.myoffer.network.base.MyOfferBaseAd;
import com.anythink.myoffer.ui.SplashAdView;
import com.anythink.network.myoffer.MyOfferError;
import com.anythink.network.myoffer.MyOfferErrorCode;

import java.util.Map;

public class MyOfferSplashAd extends MyOfferBaseAd {

    MyOfferAdListener mListener;
    MyOfferAd mMyOfferAd;

    String mRequestId;

    public MyOfferSplashAd(Context context, String placementId, String offerId, MyOfferSetting myoffer_setting, String requestId, boolean isDefault) {
        super(context, placementId, offerId, myoffer_setting, isDefault);
        mRequestId = requestId;
    }


    @Override
    public void load() {
        try {
            if (TextUtils.isEmpty(mOfferId) || TextUtils.isEmpty(mPlacementId)) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(MyOfferErrorCode.get(MyOfferErrorCode.noADError, MyOfferErrorCode.fail_params));
                }
                return;
            }
            mMyOfferAd = MyOfferAdManager.getInstance(mContext).getAdCache(mPlacementId, mOfferId);
            if (mMyOfferAd == null) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(MyOfferErrorCode.get(MyOfferErrorCode.noADError, MyOfferErrorCode.fail_no_offer));
                }
                return;
            }
            if (mMyOfferSetting == null) {
                if (mListener != null) {
                    mListener.onAdLoadFailed(MyOfferErrorCode.get(MyOfferErrorCode.noSettingError, MyOfferErrorCode.fail_no_setting));
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

    /**
     * Only for Splash
     *
     * @param container
     */
    public void show(final ViewGroup container) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                container.addView(new SplashAdView(container.getContext(), mPlacementId, mRequestId, mMyOfferAd, mMyOfferSetting, mListener));
            }
        });

    }

    public void setListener(MyOfferAdListener listener) {
        this.mListener = listener;
    }

    @Override
    public void show(Map<String, Object> extraMap) {

    }

    @Override
    public boolean isReady() {
        return false;
    }


    public void destory() {
        mListener = null;
    }
}
