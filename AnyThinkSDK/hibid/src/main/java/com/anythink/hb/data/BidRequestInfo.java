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
    public static final String KEY_PLACEMENT_ID = "KEY_PLACEMENT_ID";
    public static final String KEY_BIDDER_CLASS = "KEY_BIDDER_CLASS";
    public static final String KEY_PLATFORM_ID = "KEY_PLATFORM_ID";
    public static final String KEY_BANNER_SIZE = "KEY_BANNER_SIZE";

    private HashMap<String, Object> requestInfoMap = new HashMap<String, Object>();


    public BidRequestInfo(){

    }

    public Object get(String key){
        if (requestInfoMap.containsKey(key)){
            return requestInfoMap.get(key);
        }
        return null;
    }

    public void put(String key, Object value){
        if (!requestInfoMap.containsKey(key)){
            requestInfoMap.put(key,value);
        }
    }

    public String getAppId() {
        if (requestInfoMap.containsKey(KEY_APP_ID) &&
                requestInfoMap.get(KEY_APP_ID) instanceof String){
            return (String)requestInfoMap.get(KEY_APP_ID);
        }
        return null;
    }

    public String getAppKey() {
        if (requestInfoMap.containsKey(KEY_APP_KEY) &&
                requestInfoMap.get(KEY_APP_KEY) instanceof String){
            return (String)requestInfoMap.get(KEY_APP_KEY);
        }
        return null;
    }

    public String getPlacementId() {
        if (requestInfoMap.containsKey(KEY_PLACEMENT_ID) &&
                requestInfoMap.get(KEY_PLACEMENT_ID) instanceof String){
            return (String)requestInfoMap.get(KEY_PLACEMENT_ID);
        }
        return null;
    }

    public Class getBidderClass() {
        if (requestInfoMap.containsKey(KEY_BIDDER_CLASS) &&
                requestInfoMap.get(KEY_BIDDER_CLASS) instanceof Class){
            return (Class)requestInfoMap.get(KEY_BIDDER_CLASS);
        }
        return null;
    }

    public String getPlatformId() {
        if (requestInfoMap.containsKey(KEY_PLATFORM_ID) &&
                requestInfoMap.get(KEY_PLATFORM_ID) instanceof String){
            return (String)requestInfoMap.get(KEY_PLATFORM_ID);
        }
        return null;
    }

    public String getBannerSize() {
        if (requestInfoMap.containsKey(KEY_BANNER_SIZE) &&
                requestInfoMap.get(KEY_BANNER_SIZE) instanceof String){
            return (String)requestInfoMap.get(KEY_BANNER_SIZE);
        }
        return null;
    }

}
