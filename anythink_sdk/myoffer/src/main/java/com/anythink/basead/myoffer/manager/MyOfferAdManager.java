/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.myoffer.manager;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.basead.buiness.OfferResourceManager;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;
import com.anythink.basead.buiness.resource.OfferResourceLoader;
import com.anythink.basead.entity.MyOfferImpression;
import com.anythink.basead.entity.OfferErrorCode;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyOfferAdManager {
    private static MyOfferAdManager sIntance;
    private Context mContext;

    private MyOfferAdManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static MyOfferAdManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new MyOfferAdManager(context);
        }
        return sIntance;
    }

    public void preloadOfferList(final String placementId) {
//        TaskManager.getInstance().run_proxy(new Runnable() {
//            @Override
//            public void run() {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mContext).getPlaceStrategyByAppIdAndPlaceId(placementId);

        if (placeStrategy == null) {
            return;
        }

        List<MyOfferAd> myOfferAdList = placeStrategy.getMyOfferAdList();

        if (myOfferAdList == null) {
            return;
        }
        /**
         * PreLoad Resource
         */
        MyOfferSetting setting = placeStrategy.getMyOfferSetting();
        if (setting == null) {
            return;
        }
        OfferResourceManager.getInstance().preLoadOfferList(placementId, myOfferAdList, setting);
    }


    /**
     * Get Offer in Caches
     *
     * @param toponPlacementId
     * @param offerId
     * @return
     */
    public MyOfferAd getAdCache(String toponPlacementId, String offerId) {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mContext).getPlaceStrategyByAppIdAndPlaceId(toponPlacementId);
        if (placeStrategy == null) {
            return null;
        }

        MyOfferAd myOfferAd = placeStrategy.getMyOfferByOfferId(offerId);
        return myOfferAd;
    }


    /**
     * Get default offer order by cap
     */
    public String getDefaultCacheOfferId(String placementId) {
        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(mContext).getPlaceStrategyByAppIdAndPlaceId(placementId);
        if (placeStrategy == null) {
            return "";
        }

        List<MyOfferAd> myOfferAdList = placeStrategy.getMyOfferAdList();
        List<MyOfferImpression> myOfferImpressionList = new ArrayList<>();
        if (myOfferAdList == null || myOfferAdList.size() == 0) {
            return "";
        }

        /**
         * Check MyOffer Resource
         */
        for (int index = myOfferAdList.size() - 1; index >= 0; index--) {
            MyOfferAd myOfferAd = myOfferAdList.get(index);
            if (!OfferResourceManager.getInstance().isExist(myOfferAd, placeStrategy.getMyOfferSetting())) {
                myOfferAdList.remove(index);
            } else {
                myOfferImpressionList.add(MyOfferImpressionRecordManager.getInstance(mContext).getOfferImpreesion(myOfferAd));
            }
        }

        if (myOfferImpressionList == null || myOfferImpressionList.size() == 0) {
            return "";
        }

        Collections.sort(myOfferImpressionList, new Comparator<MyOfferImpression>() {
            @Override
            public int compare(MyOfferImpression myOfferImpressionA, MyOfferImpression myOfferImpressionB) {
                return ((Integer) myOfferImpressionA.showNum).compareTo(myOfferImpressionB.showNum);
            }
        });


        String offerId = myOfferImpressionList.get(0).offerId;

        return offerId;
    }

    /**
     * Get offerids in Caches
     *
     * @return
     */
    public String getCacheOfferId(String format, MyOfferSetting myOfferSetting) {
        PlaceStrategyManager placeStrategyManager = PlaceStrategyManager.getInstance(mContext);
        List<MyOfferAd> myOfferAdList = placeStrategyManager.getMyOfferListByFormat(format);
        JSONObject cacheOffers = new JSONObject();
        if (myOfferAdList != null) {
            try {
                for (MyOfferAd myOfferAd : myOfferAdList) {
                    if (OfferResourceManager.getInstance().isExist(myOfferAd, myOfferSetting)) {
                        cacheOffers.put(myOfferAd.getOfferId(), myOfferAd.getCreativeId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cacheOffers.toString();
    }


    /**
     * Download MyOffer's Resource
     */
    public void load(String placementId, MyOfferAd myOfferAd, MyOfferSetting myOfferSetting, final OfferResourceLoader.ResourceLoaderListener listener) {
        if (checkInExcludeOffer(myOfferAd)) {
            if (listener != null) {
                listener.onFailed(OfferErrorCode.get(OfferErrorCode.exclueOfferError, OfferErrorCode.fail_in_exclude_offer));
            }
            return;
        }

        if (MyOfferImpressionRecordManager.getInstance(mContext).isOfferInCap(myOfferAd)) { // Cap
            if (listener != null) {
                listener.onFailed(OfferErrorCode.get(OfferErrorCode.outOfCapError, OfferErrorCode.fail_out_of_cap));
            }
            return;
        } else if (MyOfferImpressionRecordManager.getInstance(mContext).isOfferInPacing(myOfferAd)) { // Pacing
            if (listener != null) {
                listener.onFailed(OfferErrorCode.get(OfferErrorCode.inPacingError, OfferErrorCode.fail_in_pacing));
            }
            return;
        }
        OfferResourceManager.getInstance().load(placementId, myOfferAd, myOfferSetting, listener);
    }

    private boolean checkInExcludeOffer(MyOfferAd myOfferAd) {
        List<String> packageNameList = SDKContext.getInstance().getExcludeMyOfferPkgList();
        if (packageNameList != null) {
            for (String pkgName : packageNameList) {
                if (TextUtils.equals(myOfferAd.getPkgName(), pkgName)) {
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Check if MyOffer Resource exist
     *
     * @param myOfferAd
     * @param isDefault
     * @return
     */
    public boolean isReady(MyOfferAd myOfferAd, MyOfferSetting myOfferSetting, boolean isDefault) {
        if (mContext == null || myOfferAd == null) {
            return false;
        }

        if (checkInExcludeOffer(myOfferAd)) {
            return false;
        }

        if (isDefault) {
            return OfferResourceManager.getInstance().isExist(myOfferAd, myOfferSetting);
        } else {
            return !MyOfferImpressionRecordManager.getInstance(mContext).isOfferInCap(myOfferAd)
                    && !MyOfferImpressionRecordManager.getInstance(mContext).isOfferInPacing(myOfferAd)
                    && OfferResourceManager.getInstance().isExist(myOfferAd, myOfferSetting);
        }

    }


}
