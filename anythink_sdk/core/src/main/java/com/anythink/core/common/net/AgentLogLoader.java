/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.net;


import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class AgentLogLoader extends AbsHttpLoader {


    Context mContext;
    int mCount;
    String mAppID;
    String mAppKey;
    List<String> mDataList;
    int reportType;

    public AgentLogLoader(Context c, int reportType, List<String> dataList) {
        super();
        mContext = c;
        mAppID = SDKContext.getInstance().getAppId();
        mAppKey = SDKContext.getInstance().getAppKey();
        mDataList = dataList;
        mCount = dataList.size();
        this.reportType = reportType;
    }


    @Override
    protected int onPrepareType() {
        return ApiRequestParam.POST;
    }

    // URL
    @Override
    protected String onPrepareURL() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(mContext).getAppStrategyByAppId(mAppID);
        String url = Const.API.URL_AGENT;
        if (appStrategy != null && !TextUtils.isEmpty(appStrategy.getDaAddress())) {
            url = appStrategy.getDaAddress();
        }
        return url;
    }

    @Override
    protected Map<String, String> onPrepareHeaders() {
        Map<String, String> maps = new HashMap<>();
        maps.put("Content-Encoding", "gzip");
        maps.put("Content-Type", "application/json;charset=utf-8");
        return maps;
    }

    @Override
    protected byte[] onPrepareContent() {
        return compress(getReqParam());
    }

    @Override
    protected boolean onParseStatusCode(int code) {
        return false;
    }

    @Override
    protected Map<String, Object> reqParamEx() {
        return null;
    }

    @Override
    protected String getAppId() {
        return mAppID;
    }

    @Override
    protected Context getContext() {
        return this.mContext;
    }

    @Override
    protected String getAppKey() {
        return mAppKey;
    }

    @Override
    protected String getApiVersion() {
        return Const.API.APPSTR_APIVERSION;
    }

    @Override
    protected JSONObject getBaseInfoObject() {
        JSONObject jsonObject = super.getBaseInfoObject();
        if (jsonObject != null) {
            try {
                jsonObject.put(ApiRequestParam.JSON_REQUEST_APPID, mAppID);
                jsonObject.put(ApiRequestParam.JSON_REQUEST_COMMON_NW_VERSION, CommonDeviceUtil.getAllNetworkVersion());

                Map<String, Object> customMap = SDKContext.getInstance().getCustomMap();
                try {
                    if (customMap != null && customMap.size() > 0) {
                        if (customMap != null) {
                            JSONObject customObject = new JSONObject();
                            for (String key : customMap.keySet()) {
                                Object itemObject = customMap.get(key);
                                if (itemObject != null) {
                                    customObject.put(key, itemObject.toString());
                                }
                            }
                            jsonObject.put(ApiRequestParam.JSON_REQUEST_CUSTOM, customObject);
                        }
                    }
                } catch (Throwable e) {

                }

                JSONArray jsonArray = new JSONArray();
                if (mDataList != null && mDataList.size() > 0) {
                    for (String logStr : mDataList) {
                        if (!TextUtils.isEmpty(logStr)) {
                            JSONObject object = new JSONObject(logStr);
                            jsonArray.put(object);
                        }
                    }
                }
                jsonObject.put(ApiRequestParam.JSON_REQUEST_DATA_LIST, jsonArray);

            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }

        }
        return jsonObject;
    }

    @Override
    protected JSONObject getMainInfoObject() {
        JSONObject jsonObject = super.getMainInfoObject();
        if (jsonObject != null) {
            try {
                jsonObject.put(ApiRequestParam.JSON_REQUEST_TKDA_REPORT_TYPE, reportType);
            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        return jsonObject;
    }

    @Override
    protected Object onParseResponse(Map<String, List<String>> headers, String jsonString) throws IOException {
        return mCount;
    }

    @Override
    protected void handleSaveHttpRequest(AdError adError) {

    }


    private String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {

        }
        return null;
    }

    @Override
    protected void onErrorAgent(String msg, AdError adError) {

    }
}
