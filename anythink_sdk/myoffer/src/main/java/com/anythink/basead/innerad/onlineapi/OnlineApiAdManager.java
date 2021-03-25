/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad.onlineapi;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.OfferResourceManager;
import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.entity.OfferError;
import com.anythink.basead.entity.OfferErrorCode;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.innerad.utils.OwnAdSettingUpdateUtil;
import com.anythink.basead.innerad.utils.OwnOfferImpressionRecordManager;
import com.anythink.basead.net.OnlineOfferLoader;
import com.anythink.core.api.AdError;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.OnlineApiOffer;
import com.anythink.core.common.net.OnHttpLoaderListener;

import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public class OnlineApiAdManager {
    private Context mContext;
    private static OnlineApiAdManager sIntance;

    ConcurrentHashMap<String, Boolean> offerLoadingStatus;

    private OnlineApiAdManager(Context context) {
        mContext = context.getApplicationContext();
        offerLoadingStatus = new ConcurrentHashMap<>(3);
    }

    public synchronized static OnlineApiAdManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new OnlineApiAdManager(context);
        }
        return sIntance;
    }


    public void loadAd(BaseAdRequestInfo onlineApiRequestInfo, OnlineApiAdManager.OnlineApiAdLoadListener onlineApiAdListener) {
        String onlineApiId = OnlineApiAdCacheManager.getInstance().getOnlineApiSaveId(onlineApiRequestInfo);
        if (offerLoadingStatus.contains(onlineApiId) && offerLoadingStatus.get(onlineApiId)) {
            if (onlineApiAdListener != null) {
                onlineApiAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.loadingError, OfferErrorCode.fail_offer_loading));
            }
            return;
        }
        offerLoadingStatus.put(onlineApiId, true);
        requestAdData(onlineApiRequestInfo, onlineApiAdListener);
    }

    private void requestAdData(final BaseAdRequestInfo onlineApiRequestInfo, final OnlineApiAdManager.OnlineApiAdLoadListener onlineApiAdListener) {
        OnlineApiOffer onlineApiOffer = null;
        try {
            onlineApiOffer = getOfferFromDiskCache(onlineApiRequestInfo);
        } catch (Throwable e) {

        }

        if (onlineApiOffer == null || onlineApiOffer.isExpire()) {
            int adWidth = 0;
            int adHeight = 0;
            if (!TextUtils.isEmpty(onlineApiRequestInfo.baseAdSetting.getBannerSize())) {
                try {
                    String[] bannerSize = onlineApiRequestInfo.baseAdSetting.getBannerSize().split("x");
                    adWidth = Integer.parseInt(bannerSize[0]);
                    adHeight = Integer.parseInt(bannerSize[1]);
                } catch (Throwable e) {

                }
            }
            String[] excludeImpressionOffer = OwnOfferImpressionRecordManager.getInstance()
                    .getOfferImpressionList(mContext
                            , OwnOfferImpressionRecordManager.getRecordId(onlineApiRequestInfo.placementId, onlineApiRequestInfo.adsourceId));
            //TODO Only Support AdNum=1 Ad Request, if AdRequest support AdNum>1, you should motify the OnlineApiAdCacheManager
            OnlineOfferLoader onlineApiOfferLoader = new OnlineOfferLoader(onlineApiRequestInfo, adWidth, adHeight, excludeImpressionOffer);
            onlineApiOfferLoader.start(0, new OnHttpLoaderListener() {
                @Override
                public void onLoadStart(int reqCode) {

                }

                @Override
                public void onLoadFinish(int reqCode, Object result) {
                    OnlineApiOffer onlineApiOffer = null;
                    JSONObject resultAdJSONObject = null;
                    try {
                        resultAdJSONObject = new JSONObject(result.toString());
                        resultAdJSONObject.put(OnlineApiParseUtils.SDK_UPDATE_TIME_KEY, System.currentTimeMillis());
                        onlineApiOffer = OnlineApiParseUtils.parseOffer(onlineApiRequestInfo, resultAdJSONObject);
                    } catch (Exception e) {

                    }

                    if (onlineApiOffer != null) {
                        //Target not installed App
                        if (onlineApiOffer.getDeeplinkTarget() == OnlineApiOffer.DEEPLINK_TARGET_NON_INSTALL_APP) {
                            boolean hasInstall = OfferAdFunctionUtil.isApkInstalled(mContext.getApplicationContext(), onlineApiOffer.getPkgName());
                            if (hasInstall) {
                                OwnOfferImpressionRecordManager.getInstance().recordOfferImpression(mContext, OwnOfferImpressionRecordManager.getRecordId(onlineApiRequestInfo.placementId, onlineApiRequestInfo.adsourceId), onlineApiOffer, onlineApiRequestInfo.baseAdSetting);
                                if (onlineApiAdListener != null) {
                                    onlineApiAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.noADError, "Application installed."));
                                }
                                return;
                            }
                        }

                        //Target installed App
                        if (onlineApiOffer.getDeeplinkTarget() == OnlineApiOffer.DEEPLINK_TARGET_INSTALLED_APP) {
                            boolean hasInstall = OfferAdFunctionUtil.isApkInstalled(mContext.getApplicationContext(), onlineApiOffer.getPkgName());
                            if (!hasInstall) {
                                OwnOfferImpressionRecordManager.getInstance().recordOfferImpression(mContext, OwnOfferImpressionRecordManager.getRecordId(onlineApiRequestInfo.placementId, onlineApiRequestInfo.adsourceId), onlineApiOffer, onlineApiRequestInfo.baseAdSetting);

                                if (onlineApiAdListener != null) {
                                    onlineApiAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.noADError, "Application not installed yet."));
                                }
                                return;
                            }
                        }

                        //update online offer setting
                        OwnAdSettingUpdateUtil.update(onlineApiRequestInfo, onlineApiOffer);
                        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.NOTICE_WIN_TYPE, onlineApiOffer, new UserOperateRecord(onlineApiRequestInfo.requestId, ""));

                        OnlineApiAdCacheManager.getInstance().saveOnlineApiOffer(mContext,OnlineApiAdCacheManager.getInstance().getOnlineApiSaveId(onlineApiRequestInfo), resultAdJSONObject.toString());
                        if (onlineApiAdListener != null) {
                            onlineApiAdListener.onAdDataLoaded(onlineApiOffer);
                        }
                        requestOfferResource(onlineApiOffer, onlineApiRequestInfo, onlineApiAdListener);
                    } else {
                        if (onlineApiAdListener != null) {
                            onlineApiAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.noADError, result != null ? result.toString() : "No Ad Return."));
                        }
                    }

                }

                @Override
                public void onLoadError(int reqCode, String msg, AdError errorCode) {
                    if (onlineApiAdListener != null) {
                        onlineApiAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.noADError, msg));
                    }
                }

                @Override
                public void onLoadCanceled(int reqCode) {
                    if (onlineApiAdListener != null) {
                        onlineApiAdListener.onAdError(OfferErrorCode.get(OfferErrorCode.noADError, "Cancel Request."));
                    }
                }
            });
        } else {
            if (onlineApiAdListener != null) {
                onlineApiAdListener.onAdDataLoaded(onlineApiOffer);
            }
            requestOfferResource(onlineApiOffer, onlineApiRequestInfo, onlineApiAdListener);
        }
    }

    private void requestOfferResource(final OnlineApiOffer onlineApiOffer, final BaseAdRequestInfo adxRequestInfo, final OnlineApiAdManager.OnlineApiAdLoadListener onlineApiAdListener) {
        OfferResourceManager.getInstance().load(adxRequestInfo.placementId, onlineApiOffer, adxRequestInfo.baseAdSetting, new OfferResourceLoader.ResourceLoaderListener() {
            @Override
            public void onSuccess() {
                offerLoadingStatus.put(OnlineApiAdCacheManager.getInstance().getOnlineApiSaveId(adxRequestInfo), false);

                if (onlineApiAdListener != null) {
                    onlineApiAdListener.onAdCacheLoaded(onlineApiOffer);
                }
            }

            @Override
            public void onFailed(OfferError msg) {
                offerLoadingStatus.put(OnlineApiAdCacheManager.getInstance().getOnlineApiSaveId(adxRequestInfo), false);
                if (onlineApiAdListener != null) {
                    onlineApiAdListener.onAdError(msg);
                }
            }
        });
    }

    public OnlineApiOffer getOfferFromDiskCache(final BaseAdRequestInfo onlineApiRequestInfo) {
        String onlineApiId = OnlineApiAdCacheManager.getInstance().getOnlineApiSaveId(onlineApiRequestInfo);
        OnlineApiOffer onlineApiOffer = null;
        String onlineApiOfferData = OnlineApiAdCacheManager.getInstance().getOnlineApiOffer(mContext, onlineApiId);
        if (TextUtils.isEmpty(onlineApiOfferData)) {
            return null;
        }
        try {
            onlineApiOffer = OnlineApiParseUtils.parseOffer(onlineApiRequestInfo, new JSONObject(onlineApiOfferData));
        } catch (Throwable e) {

        }
        if (onlineApiOffer != null) {
            //update online offer setting
            OwnAdSettingUpdateUtil.update(onlineApiRequestInfo, onlineApiOffer);
        }

        return onlineApiOffer;
    }


    public interface OnlineApiAdLoadListener {
        void onAdDataLoaded(OnlineApiOffer onlineApiOffer);

        void onAdCacheLoaded(OnlineApiOffer onlineApiOffer);

        void onAdError(OfferError offerError);
    }
}
