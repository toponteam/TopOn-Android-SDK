/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.net;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.adx.DynamicUrlAddressManager;
import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.core.api.AdError;
import com.anythink.core.common.OffLineTkManager;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.common.entity.OwnBaseAdSetting;
import com.anythink.core.common.net.AbsHttpLoader;
import com.anythink.core.common.net.ApiRequestParam;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OwnOfferTkLoader extends AbsHttpLoader {
    String hostUrl;
    JSONObject paramsObject;

    int tkType;
    String trackJSONString;
    OwnBaseAdContent ownBaseAdContent;
    Map<String, Object> replaceMap;


    public OwnOfferTkLoader(int tkType, OwnBaseAdContent ownBaseAdContent, String trackJSONString, Map<String, Object> replaceMap) {
        this.tkType = tkType;
        this.ownBaseAdContent = ownBaseAdContent;
        this.replaceMap = replaceMap;
        this.trackJSONString = trackJSONString;
    }

    public void setScenario(String scenario) {
        try {
            if (TextUtils.isEmpty(scenario)) {
                return;
            }
            paramsObject.put("scenario", scenario);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int onPrepareType() {
        return ApiRequestParam.POST;
    }

    @Override
    protected String onPrepareURL() {
        try {
            if (!TextUtils.isEmpty(trackJSONString)) {
                paramsObject = new JSONObject(trackJSONString);

                Set<Map.Entry<String, Object>> entries = replaceMap.entrySet();
                String key;
                for (Map.Entry<String, Object> entry : entries) {
                    key = entry.getKey();
                    paramsObject.put(key, entry.getValue());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        hostUrl = DynamicUrlAddressManager.getInstance().getAdxTrackRequestUrl();

        return hostUrl;
    }

    @Override
    protected Map<String, String> onPrepareHeaders() {
        Map<String, String> maps = new HashMap<>();
        maps.put("Content-Encoding", "gzip");
        maps.put("Content-Type", "application/json;charset=utf-8");

        if (ownBaseAdContent != null) {
            OwnBaseAdSetting adxAdSetting = ownBaseAdContent.getAdSetting();
            if (adxAdSetting != null) {
                if (OfferAdFunctionUtil.isUploadUserAgent(tkType, adxAdSetting)) {
                    String defaultUA = CommonDeviceUtil.getDefaultUA();
                    if (!TextUtils.isEmpty(defaultUA)) {
                        maps.put("User-Agent", defaultUA);
                    }
                }
            }
        }
        return maps;
    }

    @Override
    protected byte[] onPrepareContent() {
        return compress(paramsObject.toString());
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
        //获取上报的信息
        JSONObject jsonObject = new JSONObject();
        Map<String, String> headMap = onPrepareHeaders();
        try {
            if (headMap != null) {
                for (String key : headMap.keySet()) {
                    jsonObject.put(key, headMap.get(key));
                }
            }
        } catch (Exception e) {

        }

        String headJsonString = jsonObject.toString();
        String content = paramsObject != null ? paramsObject.toString() : "";
        String requestUrl = onPrepareURL();
        int requestType = onPrepareType();

        OffLineTkManager.getInstance().saveRequestFailInfo(requestType, requestUrl, headJsonString, content);
        AgentEventManager.sendErrorAgent("tk", adError.getPlatformCode(), adError.getPlatformMSG(), hostUrl, "", "1", "");
    }
}
