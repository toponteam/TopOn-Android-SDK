/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.data;

import java.util.HashMap;

/**
 * Reqeust info for one bidder
 */
public class BidRequestInfo {

    public static final String KEY_APP_ID = "KEY_APP_ID";
    public static final String KEY_APP_KEY = "KEY_APP_KEY";
    public static final String KEY_UNIT_PLACEMENT_ID = "KEY_UNIT_PLACEMENT_ID";
    public static final String KEY_PLACEMENT_ID = "KEY_PLACEMENT_ID";
    public static final String KEY_BIDDER_CLASS = "KEY_BIDDER_CLASS";
    public static final String KEY_PLATFORM_ID = "KEY_PLATFORM_ID";
    public static final String KEY_BANNER_SIZE = "KEY_BANNER_SIZE";
    public static final String KEY_CUSTOM_INFO = "KEY_CUSTOM_INFO";
    public static final String KEY_ADSOURCE_ID = "KEY_ADSOURCE_ID";
    public static final String KEY_BID_TOKEN_AVAIL_TIME = "KEY_BID_TOKEN_AVAIL_TIME";

    private HashMap<String, Object> requestInfoMap = new HashMap<String, Object>();


    public BidRequestInfo() {

    }

    public Object get(String key) {
        if (requestInfoMap.containsKey(key)) {
            return requestInfoMap.get(key);
        }
        return null;
    }

    public void put(String key, Object value) {
        if (!requestInfoMap.containsKey(key)) {
            requestInfoMap.put(key, value);
        }
    }

    public String getString(String key) {
        if (requestInfoMap.containsKey(key) &&
                requestInfoMap.get(key) instanceof String) {
            return (String) requestInfoMap.get(key);
        }
        return null;
    }

    public long getLong(String key) {
        if (requestInfoMap.containsKey(key)) {
            Object obj = requestInfoMap.get(key);

            if (obj instanceof Long) {
                return (long) obj;
            }
        }
        return 0;
    }

    public String getAppId() {
        return getString(KEY_APP_ID);
    }

    public String getAppKey() {
        return getString(KEY_APP_KEY);
    }

    public String getUnitPlacementId() {
        if (requestInfoMap.containsKey(KEY_UNIT_PLACEMENT_ID) &&
                requestInfoMap.get(KEY_UNIT_PLACEMENT_ID) instanceof String) {
            return (String) requestInfoMap.get(KEY_UNIT_PLACEMENT_ID);
        }
        return "";
    }

    public String getPlacementId() {
        return getString(KEY_PLACEMENT_ID);
    }

    public String getCustomInfo() {
        return getString(KEY_CUSTOM_INFO);
    }

    public Class getBidderClass() {
        if (requestInfoMap.containsKey(KEY_BIDDER_CLASS) &&
                requestInfoMap.get(KEY_BIDDER_CLASS) instanceof Class) {
            return (Class) requestInfoMap.get(KEY_BIDDER_CLASS);
        }
        return null;
    }

    public String getPlatformId() {
        return getString(KEY_PLATFORM_ID);
    }

    public String getBannerSize() {
        return getString(KEY_BANNER_SIZE);
    }

}
