/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.net.socket;

import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.common.OffLineTkManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.entity.AdTrackingLogBean;
import com.anythink.core.common.net.ApiRequestParam;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonBase64Util;
import com.anythink.core.common.utils.CommonMD5;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class TrackingSocketData extends SocketUploadData {
    private final String TAG = getClass().getSimpleName();
    List<AdTrackingLogBean> tkList;
    String offlineData;
    boolean isOfflineData;

    public TrackingSocketData(List<AdTrackingLogBean> logs) {
        tkList = logs;
    }

    public TrackingSocketData(String offlineData) {
        if (Const.DEBUG) {
            Log.e(TAG, "init offline data....");
        }
        this.isOfflineData = true;
        this.offlineData = offlineData;
    }

    @Override
    public int getApiType() {
        return TK_TYPE;
    }

    @Override
    public int getDataType() {
        return GZIP_DATA_TYPE;
    }

    @Override
    public byte[] getContentData() {
        if (!TextUtils.isEmpty(offlineData)) {
            return compress(offlineData);
        }

        if (tkList != null && tkList.size() > 0) {
            return compress(assemblingRequestInfo(false));
        }
        return new byte[0];
    }

    @Override
    public boolean isOfflineData() {
        return isOfflineData;
    }

    @Override
    public void handleLogToRequestNextTime(String errorCode, String errorMsg, String domain, int port) {

        if (!isOfflineData) {
            if (Const.DEBUG) {
                Log.e(TAG, "Save Tcp tk offline data.....");
            }

            AgentEventManager.sendErrorAgent("tk", errorCode, errorMsg, domain + ":" + port, null
                    , String.valueOf(tkList != null ? tkList.size() : 0)
                    , String.valueOf(AgentEventManager.REQUEST_TCP_TYPE));

            JSONObject jsonObject = new JSONObject();

            String headJsonString = "";
            String content = assemblingRequestInfo(true);
            String requestUrl = "";
            int requestType = ApiRequestParam.TCP; //TCP Type

            OffLineTkManager.getInstance().saveRequestFailInfo(requestType, requestUrl, headJsonString, content);
        }
    }

    private String assemblingRequestInfo(boolean isOffline) {
        JSONObject jsonObject = new JSONObject();


        JSONObject commonObject = super.getCommonDataObject();
        JSONObject mainObject = super.getDeviceDataObject();

        try {
            commonObject.put(ApiRequestParam.JSON_REQUEST_APPID, SDKContext.getInstance().getAppId());
            commonObject.put(ApiRequestParam.JSON_REQUEST_TKDA_REPORT_TYPE, reportType);
            commonObject.put(ApiRequestParam.JSON_REQUEST_TCP_RATE, reportRate);
            Iterator<String> iterator = mainObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                commonObject.put(key, mainObject.opt(key));
            }
//            commonObject.put(ApiRequestParam.JSON_REQUEST_GDPR_LEVEL, String.valueOf(UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).getUploadDataLevel()));
        } catch (JSONException e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }


        String commonEncode = CommonBase64Util.base64Encode(commonObject.toString());

        JSONArray dataArray = new JSONArray();
        JSONObject dataJsonObject;
        if (tkList != null) {
            for (AdTrackingLogBean logBean : tkList) {
                dataJsonObject = logBean.toJSONObject();
                if (isOffline && dataJsonObject != null) {
                    try {
                        dataJsonObject.put(ApiRequestParam.JSON_REQUEST_TK_OFFLINE_TAG, 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                dataArray.put(dataJsonObject);
            }
        }

        String dataEncode = CommonBase64Util.base64Encode(dataArray.toString());
        String sign = CommonMD5.getLowerMd5(SDKContext.getInstance().getAppKey()
                + ApiRequestParam.JSON_REQUEST_API_VERSION + "=" + Const.API.APPSTR_APIVERSION
                + "&" + ApiRequestParam.JSON_REQUEST_COMMON + "=" + commonEncode
                + "&" + ApiRequestParam.JSON_REQUEST_DATA_LIST + "=" + dataEncode);

        try {
            jsonObject.put(ApiRequestParam.JSON_REQUEST_COMMON, commonEncode);
            jsonObject.put(ApiRequestParam.JSON_REQUEST_DATA_LIST, dataEncode);
            jsonObject.put(ApiRequestParam.JSON_REQUEST_API_VERSION, Const.API.APPSTR_APIVERSION);
            jsonObject.put(ApiRequestParam.JSON_REQUEST_SIGIN, sign);
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

        if (Const.DEBUG) {
            Log.e(getClass().getSimpleName(), jsonObject.toString());
        }
        return jsonObject.toString();
    }


}
