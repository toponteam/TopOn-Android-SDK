/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.basead.myoffer.manager.MyOfferAdManager;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;

public abstract class MyOfferBaseAd implements IMyOfferAd {

    public String TAG = getClass().getSimpleName();

    protected Context mContext;
    protected String mPlacementId;
    protected String mOfferId;
    protected MyOfferSetting mMyOfferSetting;
    protected boolean mIsDefault;
    protected MyOfferAd mMyOfferAd;

    public static final String EXTRA_REQUEST_ID = "extra_request_id";
    public static final String EXTRA_SCENARIO = "extra_scenario";
    public static final String EXTRA_ORIENTATION = "extra_orientation";

    public MyOfferBaseAd(Context context, String placementId, String offerId, MyOfferSetting myOfferSetting, boolean isDefault) {
        this.mContext = context.getApplicationContext();
        this.mPlacementId = placementId;
        this.mOfferId = offerId;
        this.mIsDefault = isDefault;
        mMyOfferSetting = myOfferSetting;
    }

    protected OfferError checkLoadParams() {
        if (TextUtils.isEmpty(mOfferId) || TextUtils.isEmpty(mPlacementId)) {
            return OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_params);
        }
        mMyOfferAd = MyOfferAdManager.getInstance(mContext).getAdCache(mPlacementId, mOfferId);

        if (mMyOfferAd == null) {
            return OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_no_offer);
        }
        if (mMyOfferSetting == null) {
            return OfferErrorCode.get(OfferErrorCode.noSettingError, OfferErrorCode.fail_no_setting);
        }
        return null;
    }

    protected boolean checkIsReadyParams() {
        if (mContext == null) {
            CommonLogUtil.d(TAG, "isReady() context = null!");
            return false;
        } else if (TextUtils.isEmpty(mPlacementId)) {
            CommonLogUtil.d(TAG, "isReady() mPlacementId = null!");
            return false;
        } else if (TextUtils.isEmpty(mOfferId)) {
            CommonLogUtil.d(TAG, "isReady() mOfferId = null!");
            return false;
        }

        if (mMyOfferAd == null) {
            mMyOfferAd = MyOfferAdManager.getInstance(mContext).getAdCache(mPlacementId, mOfferId);
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
