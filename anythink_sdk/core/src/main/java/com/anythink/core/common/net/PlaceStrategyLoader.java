package com.anythink.core.common.net;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.AdError;
import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhou on 2017/12/30.
 */

public class PlaceStrategyLoader extends AbsHttpLoader {
    private static final String TAG = PlaceStrategyLoader.class.getSimpleName();

    public final static String CUSTOM_KEY = "sdk_custom";
    private String psId;
    private String sessionId;
    private String placeId;
    private Context mContext;

    private String appid;
    private String appKey;
    private String settingId;

    private Map<String, Object> customMap;
    private JSONObject customObject;

    long startTime;

    public PlaceStrategyLoader(Context mContext, String appId, String appKey, String placeId, String settingId, Map<String, Object> customMap) {
        this.appid = appId;
        this.appKey = appKey;
        this.mContext = mContext;
        this.placeId = placeId;
        this.psId = SDKContext.getInstance().getPsid();
        this.sessionId = SDKContext.getInstance().getSessionId(placeId);
        this.settingId = settingId;
        this.customMap = customMap;
    }

    @Override
    public void start(int reqCode, OnHttpLoaderListener listener) {
        startTime = System.currentTimeMillis();
        super.start(reqCode, listener);
    }

    @Override
    protected int onPrepareType() {
        return ApiRequestParam.POST;
    }

    @Override
    protected String onPrepareURL() {
        return Const.API.URL_PLACE_STRATEGY;
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
    protected Map<String, Object> reqParamEx() {
        return null;
    }

    @Override
    protected String getAppId() {
        return appid;
    }

    @Override
    protected Context getContext() {
        return mContext;
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
        JSONObject pObject = super.getBaseInfoObject();
        try {
            pObject.put("app_id", appid);
            pObject.put("pl_id", placeId);
            pObject.put("session_id", sessionId);
            pObject.put(ApiRequestParam.JSON_REQUEST_COMMON_NW_VERSION, CommonDeviceUtil.getAllNetworkVersion());
            pObject.put("exclude_myofferid", MyOfferAPIProxy.getIntance().getOutOfCapOfferIds(mContext));
            pObject.put(ApiRequestParam.JSON_REQUEST_GDPR_LEVEL, String.valueOf(UploadDataLevelManager.getInstance(mContext).getUploadDataLevel()));

            try {
                List<String> packageList = SDKContext.getInstance().getExcludeMyOfferPkgList();
                if (packageList != null && !packageList.isEmpty()) {
                    JSONArray jsonArray = new JSONArray(packageList);
                    pObject.put("ecpoffer", jsonArray);
                }
            } catch (Exception e) {
                if (Const.DEBUG) {
                    e.printStackTrace();
                }
            }

            /**Sy id**/
            String sysId = SDKContext.getInstance().getSysId();
            if (!TextUtils.isEmpty(sysId)) {
                pObject.put("sy_id", sysId);
            }

            String bkId = SDKContext.getInstance().getBkId();
            if (!TextUtils.isEmpty(bkId)) {
                pObject.put("bk_id", bkId);
            } else {
                SDKContext.getInstance().saveBkId(SDKContext.getInstance().getUpId());
                pObject.put("bk_id", SDKContext.getInstance().getUpId());
            }


            if (customMap != null) {
                JSONObject customObject = new JSONObject();
                for (String key : customMap.keySet()) {
                    Object itemObject = customMap.get(key);
                    if (itemObject != null) {
                        customObject.put(key, itemObject.toString());
                    }
                }
                this.customObject = customObject;
                pObject.put("custom", customObject);
            }
        } catch (JSONException e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return pObject;
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
            if (customMap != null) {
                jsonObject.put(CUSTOM_KEY, new JSONObject(customMap));
            }
            jsonString = jsonObject.toString();
        } catch (Exception e) {

        }
        AgentEventManager.sentHostCallbackTime("placement", placeId, startTime, System.currentTimeMillis());
        return jsonString;
    }

    @Override
    protected void handleSaveHttpRequest(AdError adError) {

    }


    @Override
    protected void onErrorAgent(String msg, AdError adError) {

        AgentEventManager.sendErrorAgent("placement", adError.getPlatformCode(), adError.getPlatformMSG(), null, placeId, "", "");
    }

}
