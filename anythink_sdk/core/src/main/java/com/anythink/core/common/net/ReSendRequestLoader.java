/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.net;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.common.entity.FailRequestInfo;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Re-send the fail tracking
 */
public class ReSendRequestLoader extends AbsHttpLoader {

    FailRequestInfo requestInfo;

    public ReSendRequestLoader(FailRequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public FailRequestInfo getRequestInfo() {
        return requestInfo;
    }

    @Override
    protected int onPrepareType() {
        return requestInfo.requestType;
    }

    @Override
    protected String onPrepareURL() {
        return requestInfo.requestUrl;
    }

    @Override
    protected Map<String, String> onPrepareHeaders() {
        String headerJsonString = requestInfo.headerJSONString;
        HashMap<String, String> headMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(headerJsonString);
            Iterator<String> keyInterator = jsonObject.keys();
            while (keyInterator.hasNext()) {
                String key = keyInterator.next();
                headMap.put(key, jsonObject.optString(key));
            }
        } catch (Exception e) {

        }

        return headMap;
    }

    @Override
    protected byte[] onPrepareContent() {
        boolean needGZIP = (requestInfo.headerJSONString != null && requestInfo.headerJSONString.contains("gzip")) ? true : false;
        JSONObject contentObject = null;
        try {
            contentObject = new JSONObject(requestInfo.content);
            contentObject.put("ofl", 1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (needGZIP) {
            byte[] content = contentObject != null ? compress(contentObject.toString()) : new byte[0];
            return content;
        } else {
            return contentObject != null ? contentObject.toString().getBytes() : new byte[0];
        }

    }

    @Override
    protected boolean onParseStatusCode(int code) {
        return false;
    }

    @Override
    protected String getAppId() {
        return null;
    }

    @Override
    protected Context getContext() {
        return null;
    }

    @Override
    protected String getAppKey() {
        return null;
    }

    @Override
    protected String getApiVersion() {
        return null;
    }

    @Override
    protected Map<String, Object> reqParamEx() {
        return null;
    }

    @Override
    protected void onErrorAgent(String msg, AdError adError) {

    }

    @Override
    protected Object onParseResponse(Map<String, List<String>> headers, String jsonString) throws IOException {
        return null;
    }

    @Override
    protected void handleSaveHttpRequest(AdError adError) {

    }
}
