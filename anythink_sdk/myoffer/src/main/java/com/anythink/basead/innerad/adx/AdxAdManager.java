/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad.adx;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.OfferResourceManager;
import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.innerad.utils.OwnAdSettingUpdateUtil;
import com.anythink.basead.net.AdxOfferLoader;
import com.anythink.core.api.AdError;
import com.anythink.core.common.adx.AdxCacheController;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.BaseAdRequestInfo;
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


    public void loadAd(BaseAdRequestInfo requestInfo, AdxAdLoadListener adxAdListener) {
        if (offerLoadingStatus.contains(requestInfo.placementId + requestInfo.bidId) && offerLoadingStatus.get(requestInfo.placementId + requestInfo.bidId)) {
            if (adxAdListener != null) {
                adxAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.loadingError, OfferErrorCode.fail_offer_loading));
            }
            return;
        }

        offerLoadingStatus.put(requestInfo.placementId + requestInfo.bidId, true);

        if (Const.FORMAT.SPLASH_FORMAT.equals(String.valueOf(requestInfo.baseAdSetting.getFormat()))) {
            requestSplashAd(requestInfo, adxAdListener);
        } else {
            requestAdData(requestInfo, adxAdListener);
        }


    }

    private void requestAdData(final BaseAdRequestInfo adxRequestInfo, final AdxAdLoadListener adxAdListener) {
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
                        OwnAdSettingUpdateUtil.update(adxRequestInfo, netAdxOffer);
                        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.NOTICE_WIN_TYPE, netAdxOffer, new UserOperateRecord(adxRequestInfo.requestId, ""));

                        AdxCacheController.getInstance().saveAdxOffer(mContext, adxRequestInfo.bidId, result.toString());
                        if (adxAdListener != null) {
                            adxAdListener.onAdDataLoaded(netAdxOffer);
                        }
                        requestOfferResource(netAdxOffer, adxRequestInfo, adxAdListener);
                    } else {
                        if (adxAdListener != null) {
                            adxAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.noADError, result != null ? result.toString() : "No Ad Return."));
                        }
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

    public void requestSplashAd(final BaseAdRequestInfo adxRequestInfo, final AdxAdLoadListener adxAdListener) {
        AdxOffer adxOffer = null;
        try {
            adxOffer = getAdxOfferFromDiskCache(adxRequestInfo);
        } catch (Throwable e) {

        }

        if (adxOffer == null) {
            if (adxAdListener != null) {
                adxAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.noADError, OfferErrorCode.fail_no_offer));
            }

        } else {
            //update adx offer setting
            OwnAdSettingUpdateUtil.update(adxRequestInfo, adxOffer);

            //send win notice
            if (!AdxCacheController.getInstance().isSendWinNotice(mContext, adxOffer.getBidId())) {
                OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.NOTICE_WIN_TYPE, adxOffer, new UserOperateRecord(adxRequestInfo.requestId, ""));

                AdxCacheController.getInstance().saveWinNotice(mContext, adxOffer.getBidId());
            }

            if (adxAdListener != null) {
                adxAdListener.onAdDataLoaded(adxOffer);
            }
            requestOfferResource(adxOffer, adxRequestInfo, adxAdListener);
        }

    }

    private void requestOfferResource(final AdxOffer adxOffer, final BaseAdRequestInfo adxRequestInfo, final AdxAdLoadListener adxAdListener) {
        OfferResourceManager.getInstance().load(adxRequestInfo.placementId, adxOffer, adxRequestInfo.baseAdSetting, new OfferResourceLoader.ResourceLoaderListener() {
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

    public AdxOffer getAdxOfferFromDiskCache(final BaseAdRequestInfo adxRequestInfo) {
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
            OwnAdSettingUpdateUtil.update(adxRequestInfo, adxOffer);
        }

        return adxOffer;
    }


    public interface AdxAdLoadListener {
        void onAdDataLoaded(AdxOffer adxOffer);

        void onAdCacheLoaded(AdxOffer adxOffer);

        void onAdError(OfferError offerError);
    }

}
