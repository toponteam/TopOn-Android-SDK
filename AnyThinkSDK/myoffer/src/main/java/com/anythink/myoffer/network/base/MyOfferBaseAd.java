package com.anythink.myoffer.network.base;

import android.content.Context;

import com.anythink.myoffer.entity.MyOfferSetting;

public abstract class MyOfferBaseAd implements IMyOfferAd {

    protected Context mContext;
    protected String mPlacementId;
    protected String mOfferId;
    protected String mMyOfferSettingJson;
    protected MyOfferSetting mMyOfferSetting;
    protected boolean mIsDefault;

    public static final String EXTRA_REQUEST_ID = "extra_request_id";
    public static final String EXTRA_SCENARIO = "extra_scenario";

    public MyOfferBaseAd(Context context, String placementId, String offerId, String myoffer_setting, boolean isDefault) {
        this.mContext = context.getApplicationContext();
        this.mPlacementId = placementId;
        this.mOfferId = offerId;
        this.mMyOfferSettingJson = myoffer_setting;
        this.mIsDefault = isDefault;

        init();
    }

    protected void init() {
        mMyOfferSetting = MyOfferSetting.parseMyOfferSetting(mMyOfferSettingJson);
    }
}
