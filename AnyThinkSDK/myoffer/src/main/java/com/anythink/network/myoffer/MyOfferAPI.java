package com.anythink.network.myoffer;

import android.content.Context;

import com.anythink.core.common.entity.MyOfferInitInfo;
import com.anythink.myoffer.buiness.MyOfferAdManager;
import com.anythink.myoffer.buiness.MyOfferImpressionRecordManager;

/**
 * MyOfferAPI
 * Keep Code Path
 */
public class MyOfferAPI {

    /**
     * Init MyOffer Resource
     *
     * @param context
     * @param myOfferInitInfo
     */
    public static void initTopOnOffer(Context context, MyOfferInitInfo myOfferInitInfo) {
        MyOfferAdManager.getInstance(context).initOfferList(myOfferInitInfo);
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
    public static String getCacheOfferIds(Context context) {
        return MyOfferAdManager.getInstance(context).getCacheOfferId();
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
