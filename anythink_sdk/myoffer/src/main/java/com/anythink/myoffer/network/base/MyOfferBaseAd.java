package com.anythink.myoffer.network.base;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.myoffer.buiness.MyOfferAdManager;
import com.anythink.network.myoffer.MyOfferError;
import com.anythink.network.myoffer.MyOfferErrorCode;

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

    protected MyOfferError checkLoadParams() {
        if (TextUtils.isEmpty(mOfferId) || TextUtils.isEmpty(mPlacementId)) {
            return MyOfferErrorCode.get(MyOfferErrorCode.noADError, MyOfferErrorCode.fail_params);
        }
        mMyOfferAd = MyOfferAdManager.getInstance(mContext).getAdCache(mPlacementId, mOfferId);

        if (mMyOfferAd == null) {
            return MyOfferErrorCode.get(MyOfferErrorCode.noADError, MyOfferErrorCode.fail_no_offer);
        }
        if (mMyOfferSetting == null) {
            return MyOfferErrorCode.get(MyOfferErrorCode.noSettingError, MyOfferErrorCode.fail_no_setting);
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
