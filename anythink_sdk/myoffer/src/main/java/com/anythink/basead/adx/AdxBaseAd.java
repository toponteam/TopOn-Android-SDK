/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.adx;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.myoffer.manager.MyOfferAdManager;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.AdxRequestInfo;


public abstract class AdxBaseAd {
    protected Context mContext;
    protected AdxRequestInfo mAdxRequestInfo;

    protected AdxOffer mAdxOffer;

    public AdxBaseAd(Context context, AdxRequestInfo adxRequestInfo) {
        this.mContext = context;
        this.mAdxRequestInfo = adxRequestInfo;
    }

    protected OfferError checkLoadParams() {
        if (mAdxRequestInfo == null || TextUtils.isEmpty(mAdxRequestInfo.placementId) || TextUtils.isEmpty(mAdxRequestInfo.bidId)) {
            return OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_params_adx);
        }
        return null;
    }

    protected OfferError checkShowParams() {
        if (mContext == null) {
            return OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_null_context);
        }

        if (mAdxOffer == null) {
            return OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_no_offer);
        }
        return null;
    }


    public abstract void load();

    public abstract void destory();

}
