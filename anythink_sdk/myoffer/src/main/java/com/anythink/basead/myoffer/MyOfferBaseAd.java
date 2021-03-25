/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.listeners.AdLoadListener;
import com.anythink.basead.myoffer.manager.MyOfferAdManager;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;

public abstract class MyOfferBaseAd implements IMyOfferAd {

    public String TAG = getClass().getSimpleName();

    protected Context mContext;
    protected BaseAdRequestInfo mRequestInfo;
    protected String mOfferId;
    protected boolean mIsDefault;
    protected MyOfferAd mMyOfferAd;

    public static final String EXTRA_REQUEST_ID = "extra_request_id";
    public static final String EXTRA_SCENARIO = "extra_scenario";
    public static final String EXTRA_ORIENTATION = "extra_orientation";

    public MyOfferBaseAd(Context context, BaseAdRequestInfo requestInfo, String offerId, boolean isDefault) {
        this.mContext = context.getApplicationContext();
        this.mRequestInfo = requestInfo;
        this.mOfferId = offerId;
        this.mIsDefault = isDefault;
    }

    @Override
    public void load(final AdLoadListener adLoadListener) {
        try {
            OfferError myOfferError = checkLoadParams();
            if (myOfferError != null) {
                if (adLoadListener != null) {
                    adLoadListener.onAdLoadFailed(myOfferError);
                }
                return;
            }

            MyOfferAdManager.getInstance(mContext).load(mRequestInfo.placementId, mMyOfferAd, mRequestInfo.baseAdSetting, new OfferResourceLoader.ResourceLoaderListener() {
                @Override
                public void onSuccess() {
                    if (adLoadListener != null) {
                        adLoadListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onFailed(OfferError error) {
                    if (adLoadListener != null) {
                        adLoadListener.onAdLoadFailed(error);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (adLoadListener != null) {
                adLoadListener.onAdLoadFailed(OfferErrorCode.get(OfferErrorCode.unknow, e.getMessage()));
            }
        }
    }

    @Override
    public boolean isReady() {
        try {
            if (checkIsReadyParams()) {
                return MyOfferAdManager.getInstance(mContext).isReady(mMyOfferAd, mRequestInfo.baseAdSetting, mIsDefault);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private OfferError checkLoadParams() {
        if (TextUtils.isEmpty(mOfferId) || TextUtils.isEmpty(mRequestInfo.placementId)) {
            return OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_params);
        }
        mMyOfferAd = MyOfferAdManager.getInstance(mContext).getAdCache(mRequestInfo.placementId, mOfferId);

        if (mMyOfferAd == null) {
            return OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_no_offer);
        }
        if (mRequestInfo.baseAdSetting == null) {
            return OfferErrorCode.get(OfferErrorCode.noSettingError, OfferErrorCode.fail_no_setting);
        }
        return null;
    }

    protected boolean checkIsReadyParams() {
        if (mContext == null) {
            CommonLogUtil.d(TAG, "isReady() context = null!");
            return false;
        } else if (TextUtils.isEmpty(mRequestInfo.placementId)) {
            CommonLogUtil.d(TAG, "isReady() mPlacementId = null!");
            return false;
        } else if (TextUtils.isEmpty(mOfferId)) {
            CommonLogUtil.d(TAG, "isReady() mOfferId = null!");
            return false;
        }

        if (mMyOfferAd == null) {
            mMyOfferAd = MyOfferAdManager.getInstance(mContext).getAdCache(mRequestInfo.placementId, mOfferId);
            if (mMyOfferAd == null) {
                CommonLogUtil.d(TAG, "isReady() MyOffer no exist!");
                return false;
            }
        }
        return true;
    }

    public void destroy() {

    }

}
