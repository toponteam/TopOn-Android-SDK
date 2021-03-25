/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb.adx;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.net.AbsHttpLoader;
import com.anythink.core.common.net.ApiRequestParam;
import com.anythink.core.common.utils.CommonBase64Util;
import com.anythink.core.hb.adx.network.BaseNetworkInfo;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BidRequest extends AbsHttpLoader {

    private final String HB_LIST = "hb_list";
    private final String REQUEST_ID = "request_id";
    private final String CH_INFO = "ch_info";

    public static final String NETWORK_SDK_VERSION = "display_manager_ver";
    public static final String UNIT_ID = "unit_id";
    public static final String APP_ID = "app_id";
    public static final String NW_FIRM_ID = "nw_firm_id";
    public static final String BUYERUID = "buyeruid";
    public static final String FORMAT = "ad_format";
    public static final String AD_WIDTH = "ad_width";
    public static final String AD_HEIGHT = "ad_height";

    /**
     * Adx
     */
    public static final String EXCLUDE_OFFER = "ecpoffer";
    public static final String GET_OFFER = "get_offer";

    String requestId;
    String hbListBase64;
    String extraInfoBase64;
    String bidRequestUrl;
    String placementId;

    int trafficGroupId;
    int groupId;


    public BidRequest(String bidRequestUrl, String placementId, String requestId, List<BaseNetworkInfo> baseNetworkInfoList, String extraInfo) {
        this.requestId = requestId;
        this.bidRequestUrl = bidRequestUrl;
        this.placementId = placementId;

        JSONArray jsonArray = new JSONArray();
        for (BaseNetworkInfo baseNetworkInfo : baseNetworkInfoList) {
            JSONObject networkObject = baseNetworkInfo.toRequestJSONObject();
            jsonArray.put(networkObject);
        }

        hbListBase64 = CommonBase64Util.encode(jsonArray.toString().getBytes());
        extraInfoBase64 = CommonBase64Util.encode(extraInfo.getBytes());

        PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);
        if (placeStrategy != null) {
            trafficGroupId = placeStrategy.getTracfficGroupId();
            groupId = placeStrategy.getGroupId();
        }
    }

    @Override
    protected int onPrepareType() {
        return ApiRequestParam.POST;
    }

    @Override
    protected String onPrepareURL() {
        return bidRequestUrl;
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
        return getReqParam().getBytes();
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
    protected JSONObject getBaseInfoObject() {
        JSONObject jsonObject = super.getBaseInfoObject();
        try {
            jsonObject.put(ApiRequestParam.JSON_REQUEST_APPID, SDKContext.getInstance().getAppId());
            jsonObject.put("pl_id", placementId);
            jsonObject.put("session_id", SDKContext.getInstance().getSessionId(placementId));
            jsonObject.put("t_g_id", trafficGroupId);
            jsonObject.put("gro_id", groupId);

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

        params.put(HB_LIST, hbListBase64);
        params.put(REQUEST_ID, requestId);
        params.put(CH_INFO, extraInfoBase64);


        Set<String> keys = params.keySet();
        JSONObject jsonObject = new JSONObject();
        try {
            for (String key : keys) {
                jsonObject.put(key, String.valueOf(params.get(key)));
            }
            return jsonObject.toString();
        } catch (Exception e) {

        } catch (OutOfMemoryError oom) {
            System.gc();
        }
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
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray hbJSONArray = jsonObject.optJSONArray("data");
            return hbJSONArray;
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    protected void handleSaveHttpRequest(AdError error) {

    }
}
