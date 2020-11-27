/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.net;

import android.content.Context;

import com.anythink.core.api.AdError;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CommonNoticeUrlLoader extends AbsHttpLoader {
    String url;

    public CommonNoticeUrlLoader(String url) {
        this.url = url;
    }

    @Override
    protected int onPrepareType() {
        return ApiRequestParam.GET;
    }

    @Override
    protected String onPrepareURL() {
        return url;
    }

    @Override
    protected Map<String, String> onPrepareHeaders() {
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
