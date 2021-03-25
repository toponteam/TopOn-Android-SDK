/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.basead.innerad.adx.AdxAdManager;
import com.anythink.basead.buiness.resource.OfferResourceState;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.innerad.utils.OwnAdSettingUpdateUtil;
import com.anythink.basead.listeners.AdLoadListener;
import com.anythink.basead.innerad.onlineapi.OnlineApiAdManager;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.OnlineApiOffer;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.common.entity.OwnBaseAdSetting;

public abstract class OwnBaseAd {
    public enum OFFER_TYPE {
        ADX_OFFER_REQUEST_TYPE,
        ONLINE_API_OFFER_REQUEST_TYPE
    }

    private OFFER_TYPE offerType;

    protected Context mContext;
    protected BaseAdRequestInfo mOwnBaseAdRequestInfo;
    protected OwnBaseAdConfig mOwnBaseAdConfig;

    protected OwnBaseAdContent mBaseAdContent;


    public OwnBaseAd(Context context, OFFER_TYPE offerType, BaseAdRequestInfo ownBaseAdRequestInfo) {
        this.mContext = context;
        this.offerType = offerType;
        this.mOwnBaseAdRequestInfo = ownBaseAdRequestInfo;
    }

    public void setAdConfig(OwnBaseAdConfig adxAdConfig) {
        this.mOwnBaseAdConfig = adxAdConfig;
        if (mOwnBaseAdRequestInfo.baseAdSetting instanceof OwnBaseAdSetting) {
            OwnAdSettingUpdateUtil.update((OwnBaseAdSetting) mOwnBaseAdRequestInfo.baseAdSetting, mOwnBaseAdConfig);
        }

    }

    public boolean isAdReady() {
        switch (offerType) {
            case ADX_OFFER_REQUEST_TYPE:
                if (mBaseAdContent == null) {
                    mBaseAdContent = AdxAdManager.getInstance(mContext).getAdxOfferFromDiskCache(mOwnBaseAdRequestInfo);
                }

                if (mBaseAdContent != null && OfferResourceState.isExist(mBaseAdContent, mOwnBaseAdRequestInfo.baseAdSetting)) {
                    return true;
                }
                return false;
            case ONLINE_API_OFFER_REQUEST_TYPE:
                if (mBaseAdContent == null) {
                    OnlineApiOffer onlineApiOffer = OnlineApiAdManager.getInstance(mContext).getOfferFromDiskCache(mOwnBaseAdRequestInfo);
                    if (!onlineApiOffer.isExpire()) {
                        mBaseAdContent = onlineApiOffer;
                    }
                }

                if (mBaseAdContent != null && OfferResourceState.isExist(mBaseAdContent, mOwnBaseAdRequestInfo.baseAdSetting)) {
                    return true;
                }

                return false;
        }
        return false;

    }

    /**
     * @param adLoadListener
     */
    public void load(AdLoadListener adLoadListener) {
        switch (offerType) {
            case ADX_OFFER_REQUEST_TYPE:
                requestAdxOffer(adLoadListener);
                break;
            case ONLINE_API_OFFER_REQUEST_TYPE:
                requestOnlineApiOffer(adLoadListener);
                break;
        }
    }


    protected String getAdEventId(OwnBaseAdContent ownBaseAdContent) {
        return mOwnBaseAdRequestInfo.placementId + mOwnBaseAdRequestInfo.adsourceId + mOwnBaseAdRequestInfo.networkFirmId + ownBaseAdContent.getOfferId() + System.currentTimeMillis();
    }

    /**
     * Request Adx Offer
     *
     * @param adLoadListener
     */
    private void requestAdxOffer(final AdLoadListener adLoadListener) {
        try {
            if (mOwnBaseAdRequestInfo == null || TextUtils.isEmpty(mOwnBaseAdRequestInfo.placementId) || TextUtils.isEmpty(mOwnBaseAdRequestInfo.bidId)) {
                if (adLoadListener != null) {
                    adLoadListener.onAdLoadFailed(OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_params_adx));
                }
                return;
            }

            AdxAdManager.getInstance(mContext).loadAd(mOwnBaseAdRequestInfo, new AdxAdManager.AdxAdLoadListener() {
                @Override
                public void onAdDataLoaded(AdxOffer adxOffer) {
                    if (adLoadListener != null) {
                        adLoadListener.onAdDataLoaded();
                    }
                }

                @Override
                public void onAdCacheLoaded(AdxOffer adxOffer) {
                    mBaseAdContent = adxOffer;
                    if (adLoadListener != null) {
                        adLoadListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onAdError(OfferError offerError) {
                    if (adLoadListener != null) {
                        adLoadListener.onAdLoadFailed(offerError);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            if (adLoadListener != null) {
                adLoadListener.onAdLoadFailed(OfferErrorCode.get(OfferErrorCode.unknow, e.getMessage()));
            }
        }
    }


    private void requestOnlineApiOffer(final AdLoadListener adLoadListener) {
        try {
            OnlineApiAdManager.getInstance(mContext).loadAd(mOwnBaseAdRequestInfo, new OnlineApiAdManager.OnlineApiAdLoadListener() {
                @Override
                public void onAdDataLoaded(OnlineApiOffer onlineApiOffer) {
                    if (adLoadListener != null) {
                        adLoadListener.onAdDataLoaded();
                    }
                }

                @Override
                public void onAdCacheLoaded(OnlineApiOffer onlineApiOffer) {
                    mBaseAdContent = onlineApiOffer;
                    if (adLoadListener != null) {
                        adLoadListener.onAdCacheLoaded();
                    }
                }

                @Override
                public void onAdError(OfferError offerError) {
                    if (adLoadListener != null) {
                        adLoadListener.onAdLoadFailed(offerError);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            if (adLoadListener != null) {
                adLoadListener.onAdLoadFailed(OfferErrorCode.get(OfferErrorCode.unknow, e.getMessage()));
            }
        }

    }

    public BaseAdContent getBaseAdContent() {
        return mBaseAdContent;
    }

    public void destroy() {
        mBaseAdContent = null;
    }
}
