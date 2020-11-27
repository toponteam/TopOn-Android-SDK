/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.net;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.core.api.AdError;
import com.anythink.core.common.entity.AdxAdSetting;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.net.AbsHttpLoader;
import com.anythink.core.common.net.ApiRequestParam;
import com.anythink.core.common.utils.CommonDeviceUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdxNoticeUrlLoader extends AbsHttpLoader {
    int tkType;
    String url;
    AdxOffer adxOffer;
    Map<String, Object> replaceMap;

    public AdxNoticeUrlLoader(int tkType, String url, AdxOffer adxOffer, Map<String, Object> replaceMap) {
        this.tkType = tkType;
        this.url = url;
        this.adxOffer = adxOffer;
        this.replaceMap = replaceMap;
    }

    @Override
    protected int onPrepareType() {
        return ApiRequestParam.GET;
    }

    @Override
    protected String onPrepareURL() {

        if (replaceMap != null && !TextUtils.isEmpty(url)) {
            try {
                Set<Map.Entry<String, Object>> entries = replaceMap.entrySet();
                String key;
                for (Map.Entry<String, Object> entry : entries) {
                    key = entry.getKey();
                    url = url.replaceAll("\\{" + key + "\\}", (String) entry.getValue());
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    @Override
    protected Map<String, String> onPrepareHeaders() {
        if (adxOffer != null) {
            Map<String, String> maps = new HashMap<>();

            AdxAdSetting adxAdSetting = adxOffer.getAdxAdSetting();
            if (adxAdSetting != null) {
                if (OfferAdFunctionUtil.isUploadUserAgent(tkType, adxAdSetting)) {
                    String defaultUA = CommonDeviceUtil.getDefaultUA();
                    if (!TextUtils.isEmpty(defaultUA)) {
                        maps.put("User-Agent", defaultUA);
                    }
                }
            }
            return maps;
        }
        return null;
    }

    @Override
    protected byte[] onPrepareContent() {
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
    protected String getReqParam() {
        return "";
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
