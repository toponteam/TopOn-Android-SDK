/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common;

import android.text.TextUtils;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;
import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MonitoringPlatformManager {

    private final String ADJUST_KEY = "1";
    private final String APPSFLYER_KEY = "2";

    private static MonitoringPlatformManager sInstance;


    private MonitoringPlatformManager() {

    }

    public synchronized static MonitoringPlatformManager getInstance() {
        if (sInstance == null) {
            sInstance = new MonitoringPlatformManager();
        }
        return sInstance;
    }


    public void reportImpressionRevenue(AdTrackingInfo trackingInfo) {
        try {
            if (trackingInfo == null) {
                return;
            }

            PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(trackingInfo.getmPlacementId());

            if (placeStrategy != null) {
                String impressionRevenueEvent = placeStrategy.getImpressionRevenueForMonitoringPlatformString();
                JSONObject eventObject = new JSONObject(impressionRevenueEvent);

                //Adjust event
                JSONObject adjustObject = eventObject.optJSONObject(ADJUST_KEY);
                if (adjustObject != null) {
                    String eventToken = adjustObject.optString("token");
                    if (!TextUtils.isEmpty(eventToken)) {
                        String currency = trackingInfo.getmCurrency();
                        double revenue = trackingInfo.getmBidPrice() / 1000;
                        String showId = trackingInfo.getmShowId();
                        reportImpressionRevenueToAdjust(eventToken, revenue, currency, showId);
                    }
                }

                JSONObject appsflyerObject = eventObject.optJSONObject(APPSFLYER_KEY);
                if (appsflyerObject != null) {
                    int reportType = appsflyerObject.optInt("rtye");
                    try {
                        Map<String, Object> eventValue = new HashMap<String, Object>();
                        eventValue.put(AFInAppEventParameterName.ORDER_ID, trackingInfo.getmShowId());
                        eventValue.put(AFInAppEventParameterName.CONTENT_ID, trackingInfo.getmPlacementId());
                        eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, trackingInfo.getmAdType());
                        eventValue.put(AFInAppEventParameterName.REVENUE, reportType == 2 ? trackingInfo.getmBidPrice() : trackingInfo.getmBidPrice() / 1000d);
                        eventValue.put(AFInAppEventParameterName.CURRENCY, "USD");
                        AppsFlyerLib.getInstance().trackEvent(SDKContext.getInstance().getContext(), AFInAppEventType.AD_VIEW, eventValue);
                    } catch (Throwable e) {

                    }

                }
            }

//
//
//            Map<String, Map<String, String>> impressionRevenueForMonitoringPlatformMap = mImpressionRevenueDataMap;
//            for (Map.Entry<String, Map<String, String>> entry : impressionRevenueForMonitoringPlatformMap.entrySet()) {
//                if (TextUtils.equals("1", entry.getKey())) {//Adjust
//                    String currency = trackingInfo.getmCurrency();
//                    double revenue = trackingInfo.getmBidPrice() / 1000;
//                    String showId = trackingInfo.getmShowId();
//                    Map<String, String> platformParamsMap = entry.getValue();
//
//                    String token = platformParamsMap.get("token");
//                    reportImpressionRevenueToAdjust(token, revenue, currency, showId);
//                }
//            }
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    private void reportImpressionRevenueToAdjust(String token, double revenue, String currency, String showId) {
        try {
            AdjustEvent adjustEvent = new AdjustEvent(token);
            adjustEvent.setRevenue(revenue, currency);
            adjustEvent.setOrderId(showId);
            Adjust.trackEvent(adjustEvent);
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }
}
