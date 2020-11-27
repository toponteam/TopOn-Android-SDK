/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.myoffer;

import android.content.Context;

import com.anythink.core.common.entity.MyOfferInitInfo;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.basead.myoffer.manager.MyOfferAdManager;
import com.anythink.basead.myoffer.manager.MyOfferImpressionRecordManager;

/**
 * MyOfferAPI
 * Keep Code Path
 */
public class MyOfferAPI {

    /**
     * Init MyOffer Resource
     *
     * @param context
     */
    public static void preloadTopOnOffer(Context context, MyOfferInitInfo myOfferInitInfo) {

        MyOfferAdManager.getInstance(context).preloadOfferList(myOfferInitInfo.placementId);
    }

    /**
     * Get out-of-cap's offerids.
     * @return JSONArray String
     */
    public static String getOutOfCapOfferIds(Context context) {
        return MyOfferImpressionRecordManager.getInstance(context).getOutOfCapOfferIds();
    }

    /**
     * Get offerids and creativeids in MyOffer's caches
     * @return
     */
    public static String getCacheOfferIds(Context context, String format, MyOfferSetting myOfferSetting) {
        return MyOfferAdManager.getInstance(context).getCacheOfferId(format, myOfferSetting);
    }

    /**
     * Get default offerid
     *
     * @param context
     * @param toponPlacementId
     * @return
     */
    public static String getDefaultOfferId(Context context, String toponPlacementId) {
        return MyOfferAdManager.getInstance(context).getDefaultCacheOfferId(toponPlacementId);
    }

    /**
     * Check the cap state in placement
     *
     * @param context
     * @param toponPlacementId
     * @return
     */
    public static boolean checkOffersOutOfCap(Context context, String toponPlacementId) {
        return MyOfferImpressionRecordManager.getInstance(context).checkOffersOutOfCap(toponPlacementId);
    }
}
