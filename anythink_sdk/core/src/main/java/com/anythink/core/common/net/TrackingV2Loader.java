package com.anythink.core.common.net;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.common.OffLineTkManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.entity.AdTrackingLogBean;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonBase64Util;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonMD5;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2017/12/30.
 */

public class TrackingV2Loader extends AbsHttpLoader {
    private static final String TAG = TrackingV2Loader.class.getSimpleName();

    /**
     * 1:Ad Request，2:Ad Request Success，3.Ad Request Fail， 4:Ad impression，5:Ad refresh impression，6:Ad Click， 7:Video play(Discard)，8:Ad Video play，9:Ad Video playend
     * 10: sdk Load , 11: header bidding, 12: sdk load success, 13: sdk show
     */
    public static final int AD_REQUEST_TYPE = 1;
    public static final int AD_REQUEST_SUCCESS_TYPE = 2;
    public static final int AD_REQUEST_FAIL_TYPE = 3;
    public static final int AD_SHOW_TYPE = 4;
    public static final int AD_REFRESH_SHOW_TYPE = 5;
    public static final int AD_CLICK_TYPE = 6;
    public static final int AD_VIDEO_TYPE = 7;
    public static final int AD_RV_START_TYPE = 8;
    public static final int AD_RV_CLOSE_TYPE = 9;
    public static final int AD_SDK_LOAD_TYPE = 10;
    public static final int AD_HEADERBIDDING_TYPE = 11;
    public static final int AD_SDK_LOAD_SUCCESS_TYPE = 12;
    public static final int AD_SDK_SHOW_TYPE = 13;
    public static final int ADSOURCE_SORT_TYPE = 15;


    private Context mContext;

    private String appid;
    private String appKey;
    private List<AdTrackingLogBean> logBeans;
    private AdTrackingLogBean logBean;

    boolean needPutReqParamEx = false;

    private int uploadType;

    public TrackingV2Loader(Context mContext, int uploadType, List<AdTrackingLogBean> logs) {

        this.mContext = mContext;
        this.uploadType = uploadType;

        this.appid = SDKContext.getInstance().getAppId();
        this.appKey = SDKContext.getInstance().getAppKey();

        this.logBeans = logs;

    }

//    public TrackingV2Loader(Context mContext, AdTrackingLogBean log) {
//
//        this.mContext = mContext;
//
//        this.appid = SDKContext.getInstance().getAppId();
//        this.appKey = SDKContext.getInstance().getAppKey();
//
//        this.logBean = log;
//
//    }


    @Override
    protected int onPrepareType() {
        return ApiRequestParam.POST;
    }

    @Override
    protected String onPrepareURL() {
        if (logBean != null) {//点击TK
            return logBean.adTrackingInfo.getmClickTkUrl();
        }

        AppStrategy appStrategy = AppStrategyManager.getInstance(mContext).getAppStrategyByAppId(appid);
        String url = Const.API.URL_TRACKING_STRATEGY;
        if (appStrategy != null && !TextUtils.isEmpty(appStrategy.getTkAddress())) {
            url = appStrategy.getTkAddress();
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
    protected void onErrorAgent(String msg, AdError adError) {
        int failCount = 1;
        if (logBeans != null) {
            failCount = logBeans.size();
        }
        AgentEventManager.sendErrorAgent("tk", adError.getPlatformCode(), adError.getPlatformMSG(), onPrepareURL(), null, String.valueOf(failCount), String.valueOf(AgentEventManager.REQUEST_HTTP_TYPE));

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
    protected JSONObject getBaseInfoObject() {
        JSONObject commonObject = super.getBaseInfoObject();
        JSONObject mainObject = super.getMainInfoObject();

        try {
            commonObject.put(ApiRequestParam.JSON_REQUEST_APPID, appid);
            commonObject.put(ApiRequestParam.JSON_REQUEST_TKDA_REPORT_TYPE, uploadType);
            Iterator<String> iterator = mainObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                commonObject.put(key, mainObject.opt(key));
            }
            commonObject.put(ApiRequestParam.JSON_REQUEST_GDPR_LEVEL, String.valueOf(UploadDataLevelManager.getInstance(mContext).getUploadDataLevel()));
        } catch (JSONException e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return commonObject;
    }

    @Override
    protected String getReqParam() {
        JSONObject jsonObject = new JSONObject();
        String commonEncode = CommonBase64Util.base64Encode(getBaseInfoObject().toString());

        JSONArray dataArray = new JSONArray();
        JSONObject dataJsonObject;
        if (logBeans != null) {
            for (AdTrackingLogBean logBean : logBeans) {
                dataJsonObject = logBean.toJSONObject();
                putReqParamEx(dataJsonObject);
                dataArray.put(dataJsonObject);
            }
        } else if (logBean != null) {//点击TK
            dataJsonObject = logBean.toJSONObject();
            putReqParamEx(dataJsonObject);
            dataArray.put(dataJsonObject);
        }
        String dataEncode = CommonBase64Util.base64Encode(dataArray.toString());
        String sign = CommonMD5.getLowerMd5(appKey
                + ApiRequestParam.JSON_REQUEST_API_VERSION + "=" + Const.API.APPSTR_APIVERSION
                + "&" + ApiRequestParam.JSON_REQUEST_COMMON + "=" + commonEncode
                + "&" + ApiRequestParam.JSON_REQUEST_DATA_LIST + "=" + dataEncode);
        try {
            jsonObject.put(ApiRequestParam.JSON_REQUEST_COMMON, commonEncode);
            jsonObject.put(ApiRequestParam.JSON_REQUEST_DATA_LIST, dataEncode);
            jsonObject.put(ApiRequestParam.JSON_REQUEST_API_VERSION, Const.API.APPSTR_APIVERSION);
            jsonObject.put(ApiRequestParam.JSON_REQUEST_SIGIN, sign);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject.toString();

    }

    protected void putReqParamEx(JSONObject jsonObject) {
        if (needPutReqParamEx && jsonObject != null) {
            try {
                jsonObject.put("ofl", 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected Object onParseResponse(Map<String, List<String>> headers, String jsonString) throws IOException {

        jsonString = jsonString.trim();
        CommonLogUtil.i(TAG, "data:" + jsonString);
        return jsonString;
    }

    @Override
    protected void handleSaveHttpRequest(AdError adError) {
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
        needPutReqParamEx = true;
        String content = getReqParam();
        needPutReqParamEx = false;
        String requestUrl = onPrepareURL();
        int requestType = onPrepareType();

        OffLineTkManager.getInstance().saveRequestFailInfo(requestType, requestUrl, headJsonString, content);
    }
}
