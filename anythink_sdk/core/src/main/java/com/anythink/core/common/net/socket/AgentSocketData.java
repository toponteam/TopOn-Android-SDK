/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.net.socket;

import android.text.TextUtils;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.net.ApiRequestParam;
import com.anythink.core.common.utils.CommonBase64Util;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.common.utils.CommonMD5;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentSocketData extends SocketUploadData {

    List<String> agentLogList;
    int logCount;

    public AgentSocketData(List<String> logs) {
        agentLogList = logs;
        logCount = logs.size();
    }

    public int getLogCount() {
        return logCount;
    }

    @Override
    public int getApiType() {
        return AGENT_TYPE;
    }

    @Override
    public int getDataType() {
        return GZIP_DATA_TYPE;
    }

    @Override
    public byte[] getContentData() {
        return compress(assemblingRequestInfo());
    }

    @Override
    public boolean isOfflineData() {
        return false;
    }


    private String assemblingRequestInfo() {

        JSONObject commonObject = super.getCommonDataObject();
        JSONObject mainObject = super.getDeviceDataObject();

        if (commonObject != null) {
            try {
                commonObject.put(ApiRequestParam.JSON_REQUEST_APPID, SDKContext.getInstance().getAppId());
                commonObject.put(ApiRequestParam.JSON_REQUEST_COMMON_NW_VERSION, CommonDeviceUtil.getAllNetworkVersion());
                JSONArray jsonArray = new JSONArray();
                if (agentLogList != null && agentLogList.size() > 0) {
                    for (String logStr : agentLogList) {
                        if (!TextUtils.isEmpty(logStr)) {
                            JSONObject object = new JSONObject(logStr);
                            jsonArray.put(object);
                        }
                    }
                }
                commonObject.put(ApiRequestParam.JSON_REQUEST_DATA_LIST, jsonArray);

            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        if (mainObject != null) {
            try {
                mainObject.put(ApiRequestParam.JSON_REQUEST_TKDA_REPORT_TYPE, reportType);
                mainObject.put(ApiRequestParam.JSON_REQUEST_TCP_RATE, reportRate);
            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        Map<String, Object> params = new HashMap<String, Object>();
        String pEncode = CommonBase64Util.base64Encode(commonObject.toString());
        String p2Encode = CommonBase64Util.base64Encode(mainObject.toString());

        params.put(ApiRequestParam.JSON_REQUEST_API_VERSION, Const.API.APPSTR_APIVERSION);
        params.put(ApiRequestParam.JSON_REQUEST_P, pEncode);
        params.put(ApiRequestParam.JSON_REQUEST_P2, p2Encode);


        List<String> keyList = new ArrayList<>(params.size());
        keyList.addAll(params.keySet());
        Collections.sort(keyList);

        StringBuilder sb = new StringBuilder();
        for (String tmp_key : keyList) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(tmp_key);
            sb.append("=");
            sb.append(params.get(tmp_key));
        }


        String sign = CommonMD5.getLowerMd5(SDKContext.getInstance().getAppKey() + sb.toString());
        params.put(ApiRequestParam.JSON_REQUEST_SIGIN, sign);

        JSONObject jsonObject = new JSONObject(params);
        return jsonObject.toString();
    }


}
