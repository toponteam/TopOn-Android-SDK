/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.adx.manager;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.basead.adx.utils.AdxOfferParseUtil;
import com.anythink.basead.adx.utils.AdxOfferSettingUpdateUtil;
import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.OfferResourceManager;
import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.net.AdxOfferLoader;
import com.anythink.core.api.AdError;
import com.anythink.core.common.adx.AdxCacheController;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.AdxRequestInfo;
import com.anythink.core.common.net.OnHttpLoaderListener;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public class AdxAdManager {


    private Context mContext;
    private static AdxAdManager sIntance;

    ConcurrentHashMap<String, Boolean> offerLoadingStatus;

    private AdxAdManager(Context context) {
        mContext = context.getApplicationContext();
        offerLoadingStatus = new ConcurrentHashMap<>(3);
    }

    public synchronized static AdxAdManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new AdxAdManager(context);
        }
        return sIntance;
    }


    public void loadAd(AdxRequestInfo adxRequestInfo, AdxAdLoadListener adxAdListener) {
        if (offerLoadingStatus.contains(adxRequestInfo.placementId + adxRequestInfo.bidId) && offerLoadingStatus.get(adxRequestInfo.placementId + adxRequestInfo.bidId)) {
            if (adxAdListener != null) {
                adxAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.loadingError, OfferErrorCode.fail_offer_loading));
            }
            return;
        }
        offerLoadingStatus.put(adxRequestInfo.placementId + adxRequestInfo.bidId, true);
        requestAdData(adxRequestInfo, adxAdListener);
    }

    private void requestAdData(final AdxRequestInfo adxRequestInfo, final AdxAdLoadListener adxAdListener) {
        AdxOffer adxOffer = null;
        try {
            adxOffer = getAdxOfferFromDiskCache(adxRequestInfo);
        } catch (Throwable e) {

        }

        if (adxOffer == null) {
            AdxOfferLoader adxOfferLoader = new AdxOfferLoader(adxRequestInfo);
            adxOfferLoader.start(0, new OnHttpLoaderListener() {
                @Override
                public void onLoadStart(int reqCode) {

                }

                @Override
                public void onLoadFinish(int reqCode, Object result) {
                    AdxOffer netAdxOffer = null;
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        netAdxOffer = AdxOfferParseUtil.parseOffer(adxRequestInfo.bidId, jsonObject);
                    } catch (Exception e) {

                    }

                    if (netAdxOffer != null) {
                        //update adx offer setting
                        AdxOfferSettingUpdateUtil.update(adxRequestInfo, netAdxOffer);
                        OfferAdFunctionUtil.sendAdxAdTracking(OfferAdFunctionUtil.NOTICE_WIN_TYPE, netAdxOffer);

                        AdxCacheController.getInstance().saveAdxOffer(mContext, adxRequestInfo.bidId, result.toString());
                        if (adxAdListener != null) {
                            adxAdListener.onAdDataLoaded(netAdxOffer);
                        }
                        requestOfferResource(netAdxOffer, adxRequestInfo, adxAdListener);
                    }

                }

                @Override
                public void onLoadError(int reqCode, String msg, AdError errorCode) {
                    if (adxAdListener != null) {
                        adxAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.noADError, msg));
                    }
                }

                @Override
                public void onLoadCanceled(int reqCode) {
                    if (adxAdListener != null) {
                        adxAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.noADError, "Cancel Request."));
                    }
                }
            });
        } else {
            if (adxAdListener != null) {
                adxAdListener.onAdDataLoaded(adxOffer);
            }
            requestOfferResource(adxOffer, adxRequestInfo, adxAdListener);
        }
    }

    private void requestOfferResource(final AdxOffer adxOffer, final AdxRequestInfo adxRequestInfo, final AdxAdLoadListener adxAdListener) {
        OfferResourceManager.getInstance().load(adxRequestInfo.placementId, adxOffer, adxRequestInfo.adxAdSetting, new OfferResourceLoader.ResourceLoaderListener() {
            @Override
            public void onSuccess() {
                offerLoadingStatus.put(adxRequestInfo.placementId + adxRequestInfo.bidId, false);

                if (adxAdListener != null) {
                    adxAdListener.onAdCacheLoaded(adxOffer);
                }
            }

            @Override
            public void onFailed(OfferError msg) {
                offerLoadingStatus.put(adxRequestInfo.placementId + adxRequestInfo.bidId, false);
                if (adxAdListener != null) {
                    adxAdListener.onAdError(msg);
                }
            }
        });
    }

    public AdxOffer getAdxOfferFromDiskCache(final AdxRequestInfo adxRequestInfo) {
        AdxOffer adxOffer = null;
        String adxOfferData = AdxCacheController.getInstance().getAdxOffer(mContext, adxRequestInfo.bidId);
        if (TextUtils.isEmpty(adxOfferData)) {
            return null;
        }
        try {
            adxOffer = AdxOfferParseUtil.parseOffer(adxRequestInfo.bidId, new JSONObject(adxOfferData));
        } catch (Throwable e) {

        }
        if (adxOffer != null) {
            //update adx offer setting
            AdxOfferSettingUpdateUtil.update(adxRequestInfo, adxOffer);
        }

        return adxOffer;
    }


    public interface AdxAdLoadListener {
        void onAdDataLoaded(AdxOffer adxOffer);

        void onAdCacheLoaded(AdxOffer adxOffer);

        void onAdError(OfferError offerError);
    }

}
