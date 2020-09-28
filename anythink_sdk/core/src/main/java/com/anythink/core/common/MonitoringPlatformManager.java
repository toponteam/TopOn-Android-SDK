package com.anythink.core.common;

import android.text.TextUtils;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdTrackingInfo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MonitoringPlatformManager {

    private static MonitoringPlatformManager sInstance;

    private Map<String, Map<String, String>> mImpressionRevenueDataMap;

    private MonitoringPlatformManager() {

    }

    public synchronized static MonitoringPlatformManager getInstance() {
        if (sInstance == null) {
            sInstance = new MonitoringPlatformManager();
        }
        return sInstance;
    }

    public void parseMonitoringPlatformParams(String paramsJson) {
        try {
            if (TextUtils.isEmpty(paramsJson)) {
                return;
            }

            JSONObject jsonObject = new JSONObject(paramsJson);
            Map<String, Map<String, String>> map = new HashMap<>();
            Iterator<String> jsonKeyIterator = jsonObject.keys();
            String key;
            String paramsKey;
            while (jsonKeyIterator.hasNext()) {
                key = jsonKeyIterator.next();

                Map<String, String> platformMap = new HashMap<>();
                JSONObject platformJsonObject = new JSONObject(jsonObject.optString(key));
                Iterator<String> iterator = platformJsonObject.keys();
                while (iterator.hasNext()) {
                    paramsKey = iterator.next();
                    platformMap.put(paramsKey, platformJsonObject.optString(paramsKey));
                }
                map.put(key, platformMap);
            }

            mImpressionRevenueDataMap = map;
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void reportImpressionRevenue(AdTrackingInfo trackingInfo) {
        try {
            if (mImpressionRevenueDataMap == null || trackingInfo == null) {
                return;
            }

            Map<String, Map<String, String>> impressionRevenueForMonitoringPlatformMap = mImpressionRevenueDataMap;
            for (Map.Entry<String, Map<String, String>> entry : impressionRevenueForMonitoringPlatformMap.entrySet()) {
                if (TextUtils.equals("1", entry.getKey())) {//Adjust
                    String currency = trackingInfo.getmCurrency();
                    double revenue = trackingInfo.getmBidPrice() / 1000;
                    String showId = trackingInfo.getmShowId();
                    Map<String, String> platformParamsMap = entry.getValue();

                    String token = platformParamsMap.get("token");
                    reportImpressionRevenueToAdjust(token, revenue, currency, showId);
                }
            }
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
