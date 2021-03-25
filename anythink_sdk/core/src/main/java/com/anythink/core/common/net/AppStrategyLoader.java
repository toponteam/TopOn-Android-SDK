/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.net;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;

import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.common.base.AdvertisingIdClient;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.common.utils.CommonMD5;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;


public class AppStrategyLoader extends AbsHttpLoader {
    private static final String TAG = AppStrategyLoader.class.getSimpleName();

    public static final String CUSTOM_KEY = "custom";
    private String appid;
    private String appKey;
    private Context mContext;

    long startTime;
    long startElapsedRealtime;

    Map<String,Object> appCustomMap;

    public AppStrategyLoader(Context mContext, String appId, String appKey) {
        this.appid = appId;
        this.appKey = appKey;
        this.mContext = mContext;
        this.appCustomMap = SDKContext.getInstance().getCustomMap();;
    }

    @Override
    public void start(int reqCode, OnHttpLoaderListener listener) {
        startTime = System.currentTimeMillis();
        startElapsedRealtime = SystemClock.elapsedRealtime();
        super.start(reqCode, listener);
    }

    @Override
    protected int onPrepareType() {
        return ApiRequestParam.POST;
    }

    @Override
    protected String onPrepareURL() {
        return Const.API.URL_APP_STRATEGY;
    }

    @Override
    protected Map<String, String> onPrepareHeaders() {
        Map<String, String> maps = new HashMap<>();
        maps.put("Accept-Encoding", "gzip");
        maps.put("Content-Type", "application/json;charset=utf-8");
        return maps;
    }

    @Override
    protected byte[] onPrepareContent() {
        try {
            return getReqParam().getBytes("utf-8");
        } catch (Exception e) {

        }
        return getReqParam().getBytes();
    }

    @Override
    protected boolean onParseStatusCode(int code) {
        return false;
    }

    @Override
    protected String getAppId() {
        return appid;
    }

    @Override
    protected Context getContext() {
        return this.mContext;
    }

    @Override
    protected String getAppKey() {
        return appKey;
    }

    @Override
    protected String getApiVersion() {
        return Const.API.APPSTR_APIVERSION;
    }

    @Override
    protected Map<String, Object> reqParamEx() {
        return null;
    }


    @Override
    protected JSONObject getBaseInfoObject() {

        JSONObject temp = super.getBaseInfoObject();
        try {
            temp.put("app_id", appid);
            temp.put(ApiRequestParam.JSON_REQUEST_COMMON_NW_VERSION, CommonDeviceUtil.getAllNetworkVersion());
//            temp.put(ApiRequestParam.JSON_REQUEST_GDPR_LEVEL, String.valueOf(UploadDataLevelManager.getInstance(mContext).getUploadDataLevel()));

            String sysId = SDKContext.getInstance().getSysId();
            if (!TextUtils.isEmpty(sysId)) {
                temp.put("sy_id", sysId);
            }

            String bkId = SDKContext.getInstance().getBkId();
            if (!TextUtils.isEmpty(bkId)) {
                temp.put("bk_id", bkId);
            } else {
                SDKContext.getInstance().saveBkId(SDKContext.getInstance().getUpId());
                temp.put("bk_id", SDKContext.getInstance().getUpId());
            }

            //Add Custom Rule
            Map<String,Object> appCustomMap = SDKContext.getInstance().getCustomMap();
            if (appCustomMap != null) {
                JSONObject customObject = new JSONObject();
                for (String key : appCustomMap.keySet()) {
                    Object itemObject = appCustomMap.get(key);
                    if (itemObject != null) {
                        customObject.put(key, itemObject.toString());
                    }
                }
                temp.put("custom", customObject);
            }

        } catch (JSONException e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return temp;
    }

    @Override
    protected JSONObject getMainInfoObject() {
        JSONObject p2Object = super.getMainInfoObject();
        return p2Object;
    }


    @Override
    protected Object onParseResponse(Map<String, List<String>> headers, String jsonString) throws IOException {
        jsonString = jsonString.trim();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            if (appCustomMap != null) {
                jsonObject.put("custom", new JSONObject(appCustomMap));
            }
            jsonString = jsonObject.toString();
        } catch (Exception e) {

        }
        AgentEventManager.sentHostCallbackTime("app", null, startTime, System.currentTimeMillis(), SystemClock.elapsedRealtime() - startElapsedRealtime);
        return jsonString;
    }

    @Override
    protected void handleSaveHttpRequest(AdError adError) {

    }

    @Override
    protected void onErrorAgent(String msg, AdError adError) {
        AgentEventManager.sendErrorAgent("app", adError.getPlatformCode(), adError.getPlatformMSG(), null, "", "", "");
    }


}
