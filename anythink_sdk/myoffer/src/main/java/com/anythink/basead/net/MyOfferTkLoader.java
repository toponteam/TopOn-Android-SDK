/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.net;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.core.api.AdError;
import com.anythink.core.common.OffLineTkManager;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.net.AbsHttpLoader;
import com.anythink.core.common.net.ApiRequestParam;
import com.anythink.core.common.track.AgentEventManager;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyOfferTkLoader extends AbsHttpLoader {
    String hostUrl;
    JSONObject paramsObject;

    public MyOfferTkLoader(int tkType, MyOfferAd myOfferAd, String requestId) {
        try {
            String myOfferTkUrl = "";
            switch (tkType) {
                case OfferAdFunctionUtil.VIDEO_START_TYPE:
                    myOfferTkUrl = myOfferAd.getVideoStartTrackUrl();
                    break;
                case OfferAdFunctionUtil.VIDEO_PROGRESS25_TYPE:
                    myOfferTkUrl = myOfferAd.getVideoProgress25TrackUrl();
                    break;
                case OfferAdFunctionUtil.VIDEO_PROGRESS50_TYPE:
                    myOfferTkUrl = myOfferAd.getVideoProgress50TrackUrl();
                    break;
                case OfferAdFunctionUtil.VIDEO_PROGRESS75_TYPE:
                    myOfferTkUrl = myOfferAd.getVideoProgress75TrackUrl();
                    break;
                case OfferAdFunctionUtil.VIDEO_FINISH_TYPE:
                    myOfferTkUrl = myOfferAd.getVideoFinishTrackUrl();
                    break;
                case OfferAdFunctionUtil.ENDCARD_SHOW_TYPE:
                    myOfferTkUrl = myOfferAd.getEndCardShowTrackUrl();
                    break;
                case OfferAdFunctionUtil.ENDCARD_CLOSE_TYPE:
                    myOfferTkUrl = myOfferAd.getEndCardCloseTrackUrl();
                    break;
                case OfferAdFunctionUtil.IMPRESSION_TYPE:
                    myOfferTkUrl = myOfferAd.getImpressionTrackUrl();
                    break;
                case OfferAdFunctionUtil.CLICK_TYPE:
                    myOfferTkUrl = myOfferAd.getClickTrackUrl();
                    break;
            }

            myOfferTkUrl = myOfferAd.handleTKUrlReplace(myOfferTkUrl);

            Uri uri = Uri.parse(myOfferTkUrl);
            hostUrl = uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
            paramsObject = new JSONObject();
            for (String paramKey : uri.getQueryParameterNames()) {
                paramsObject.put(paramKey, URLEncoder.encode(uri.getQueryParameter(paramKey)));
            }
            paramsObject.put("req_id", requestId);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        return hostUrl;
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
        if (paramsObject != null) {
            try {
                paramsObject.put("t", String.valueOf(System.currentTimeMillis()));
            } catch (Exception e) {

            }
            return compress(paramsObject.toString());
        }
        return new byte[0];
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
    protected String getReqParam() {
        return "";
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
