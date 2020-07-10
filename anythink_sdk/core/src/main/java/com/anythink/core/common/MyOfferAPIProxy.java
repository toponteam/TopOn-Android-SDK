package com.anythink.core.common;

import android.content.Context;

import com.anythink.core.common.entity.MyOfferInitInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;

public class MyOfferAPIProxy {
    private static MyOfferAPIProxy sIntance;
    public static final int MYOFFER_NETWORK_FIRM_ID = 35;
    public static final String MYOFFER_DEFAULT_TAG = "isDefaultOffer";

    Method initTopOnOfferMethod;
    Method getOutOfCapOfferIdsMethod;
    Method getCacheOfferIdsMethod;
    Method getDefaultOfferIdMethod;
    Method getCheckOffersOutOfCapMethod;

    private MyOfferAPIProxy() {
        try {
            Class<?> myOfferAPI = Class.forName("com.anythink.network.myoffer.MyOfferAPI");
            initTopOnOfferMethod = myOfferAPI.getDeclaredMethod("initTopOnOffer", Context.class, MyOfferInitInfo.class);
            getOutOfCapOfferIdsMethod = myOfferAPI.getDeclaredMethod("getOutOfCapOfferIds", Context.class);
            getCacheOfferIdsMethod = myOfferAPI.getDeclaredMethod("getCacheOfferIds", Context.class);
            getDefaultOfferIdMethod = myOfferAPI.getDeclaredMethod("getDefaultOfferId", Context.class, String.class);
            getCheckOffersOutOfCapMethod = myOfferAPI.getDeclaredMethod("checkOffersOutOfCap", Context.class, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static synchronized MyOfferAPIProxy getIntance() {
        if (sIntance == null) {
            sIntance = new MyOfferAPIProxy();
        }
        return sIntance;
    }

    /**
     * Init MyOffer List
     *
     * @param placementId
     * @param offerListJsonArray
     */
    public void initTopOnOffer(Context context, String placementId, String offerListJsonArray, String tkInfoMap, String myofferSetting, boolean isPreLoadRes) {
        try {
            if (initTopOnOfferMethod != null) {
                MyOfferInitInfo myOfferInitInfo = new MyOfferInitInfo();
                myOfferInitInfo.placementId = placementId;
                myOfferInitInfo.offerList = offerListJsonArray;
                myOfferInitInfo.tkInfoMap = tkInfoMap;
                myOfferInitInfo.settings = myofferSetting;
                myOfferInitInfo.isNeedPreloadRes = isPreLoadRes;
                initTopOnOfferMethod.invoke(null, context, myOfferInitInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Get the MyOffer Id which out of cap
     *
     * @return
     */
    public JSONArray getOutOfCapOfferIds(Context context) {
        try {
            if (getOutOfCapOfferIdsMethod != null) {
                Object outOfCapOfferIdsObject = getOutOfCapOfferIdsMethod.invoke(null, context);
                JSONArray outOfCapOfferIdsArray = new JSONArray(outOfCapOfferIdsObject.toString());
                return outOfCapOfferIdsArray;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    /**
     * Get the MyOffer's Id which is in caches
     *
     * @return
     */
    public JSONObject getCacheOfferIds(Context context) {
        try {
            if (getCacheOfferIdsMethod != null) {
                Object cacheOfferIdsObject = getCacheOfferIdsMethod.invoke(null, context);
                JSONObject cacheOfferIdsArray = new JSONObject(cacheOfferIdsObject.toString());
                return cacheOfferIdsArray;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    /**
     * Get the default MyOffer's Id
     */
    public String getDefaultOfferId(Context context, String toponPlacementId) {
        try {
            if (getDefaultOfferIdMethod != null) {
                Object cacheOfferIdsObject = getDefaultOfferIdMethod.invoke(null, context, toponPlacementId);
                return cacheOfferIdsObject.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * Check the offer's cap status in placement
     *
     * @param context
     * @param toponPlacementId
     * @return
     */
    public boolean checkOffersOutOfCap(Context context, String toponPlacementId) {
        try {
            if (getCheckOffersOutOfCapMethod != null) {
                Object isOutOfCap = getCheckOffersOutOfCapMethod.invoke(null, context, toponPlacementId);
                return (Boolean) isOutOfCap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
