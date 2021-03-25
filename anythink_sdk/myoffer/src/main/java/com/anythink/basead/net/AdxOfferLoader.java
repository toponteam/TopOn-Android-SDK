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
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.net.AbsHttpLoader;
import com.anythink.core.common.net.ApiRequestParam;
import com.anythink.core.common.utils.CommonBase64Util;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdxOfferLoader extends AbsHttpLoader {
    String mPayload;
    String mRequestId;
    String mPlacementId;
    int mTrafficGroupId;
    int mGroupId;

    public AdxOfferLoader(BaseAdRequestInfo adxRequestInfo) {

        this.mPayload = adxRequestInfo.bidId;
        this.mRequestId = adxRequestInfo.requestId;
        this.mPlacementId = adxRequestInfo.placementId;
        this.mTrafficGroupId = adxRequestInfo.trafficGroupId;
        this.mGroupId = adxRequestInfo.groupId;
    }

    @Override
    protected int onPrepareType() {
        return ApiRequestParam.POST;
    }

    @Override
    protected String onPrepareURL() {
        return DynamicUrlAddressManager.getInstance().getAdxOfferRequestUrl();
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
        try {
            return getReqParam().getBytes("utf-8");
        } catch (Exception e) {

        }
        return getReqParam().getBytes();
    }

    @Override
    protected JSONObject getBaseInfoObject() {
        JSONObject jsonObject = super.getBaseInfoObject();
        try {
            jsonObject.put(ApiRequestParam.JSON_REQUEST_APPID, SDKContext.getInstance().getAppId());
            jsonObject.put("pl_id", mPlacementId);
            jsonObject.put("session_id", SDKContext.getInstance().getSessionId(mPlacementId));
            jsonObject.put("t_g_id", mTrafficGroupId);
            jsonObject.put("gro_id", mGroupId);

            /**Sy id**/
            String sysId = SDKContext.getInstance().getSysId();
            if (!TextUtils.isEmpty(sysId)) {
                jsonObject.put("sy_id", sysId);
            }

            String bkId = SDKContext.getInstance().getBkId();
            if (!TextUtils.isEmpty(bkId)) {
                jsonObject.put("bk_id", bkId);
            } else {
                SDKContext.getInstance().saveBkId(SDKContext.getInstance().getUpId());
                jsonObject.put("bk_id", SDKContext.getInstance().getUpId());
            }

        } catch (Exception e) {

        }
        return jsonObject;
    }

    @Override
    protected String getReqParam() {
        Map<String, Object> params = new HashMap<String, Object>();
        String pEncode = CommonBase64Util.base64Encode(getBaseInfoObject().toString());
        String p2Encode = CommonBase64Util.base64Encode(getMainInfoObject().toString());

        params.put("p", pEncode);
        params.put("p2", p2Encode);
        params.put("request_id", mRequestId);
        params.put("bid_id", mPayload);

        JSONObject jsonObject = new JSONObject(params);

        return jsonObject.toString();
    }


    @Override
    protected Object onParseResponse(Map<String, List<String>> headers, String jsonString) throws IOException {
        return jsonString;
    }

    @Override
    protected void onLoadFinishCallback(int reqCode, Object result) {
        if (result == null) {
            onErrorCallback(reqCode, "Return Empty Ad.", ErrorCode.getErrorCode(ErrorCode.noADError, "", ""));
            return;
        }

        try {
            JSONObject resultAdJSONObject = new JSONObject(result.toString());
            if (TextUtils.isEmpty(resultAdJSONObject.optString("data"))) {
                onErrorCallback(reqCode, "Return Empty Ad.", ErrorCode.getErrorCode(ErrorCode.noADError, "", result.toString()));
                return;
            }
        } catch (Throwable e) {
            onErrorCallback(reqCode, "Return Empty Ad.", ErrorCode.getErrorCode(ErrorCode.noADError, "", result != null ? result.toString() : "Adx Service Error."));
            return;
        }
        super.onLoadFinishCallback(reqCode, result);
    }

    @Override
    protected void handleSaveHttpRequest(AdError error) {

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
}
