package com.anythink.core.common.net;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import com.anythink.core.api.ATSDK;
import com.anythink.core.api.AdError;
import com.anythink.core.common.base.AdvertisingIdClient;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.common.utils.CommonMD5;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by zhou on 2017/12/30.
 */

public class AppStrategyLoader extends AbsHttpLoader {
    private static final String TAG = AppStrategyLoader.class.getSimpleName();

    private String appid;
    private String appKey;
    private Context mContext;

    long startTime;

    public AppStrategyLoader(Context mContext, String appId, String appKey) {
        this.appid = appId;
        this.appKey = appKey;
        this.mContext = mContext;
    }

    @Override
    public void start(int reqCode, OnHttpLoaderListener listener) {
        startTime = System.currentTimeMillis();
        super.start(reqCode, listener);
    }

    @Override
    protected int onPrepareType() {
        return AbsHttpLoader.POST;
    }

    @Override
    protected String onPrepareURL() {
        return Const.API.URL_APP_STRATEGY;
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
    protected Map<String, Object> reqParamEx() {
        return null;
    }


    @Override
    protected JSONObject getBaseInfoObject() {
        if (TextUtils.isEmpty(SDKContext.getInstance().getUpId())) {
            initUpId();
        }

        JSONObject temp = super.getBaseInfoObject();
        try {
            temp.put("app_id", appid);
            temp.put(JSON_REQUEST_COMMON_NW_VERSION, CommonDeviceUtil.getAllNetworkVersion());
            temp.put(JSON_REQUEST_GDPR_LEVEL, String.valueOf(UploadDataLevelManager.getInstance(mContext).getUploadDataLevel()));

        } catch (JSONException e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return temp;
    }

    @Override
    protected JSONObject getMainInfoObject() {
        JSONObject p2Object = super.getMainInfoObject();
        return p2Object;
    }


    /**
     * init upid
     */
    protected void initUpId() {
        String deviceId = "";
        if (UploadDataLevelManager.getInstance(mContext).getUploadDataLevel() == ATSDK.PERSONALIZED) {
            deviceId = getGaid();
            CommonDeviceUtil.setGoogleAdId(deviceId);
            if (TextUtils.isEmpty(deviceId) || isGaidInvalid(deviceId)) {
                deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            if (TextUtils.isEmpty(deviceId)) {
                /**if deviceid is null，create the uuid**/
                deviceId = UUID.randomUUID().toString();
            }
        } else {
            /** GDPRLevel==NONPERSONALIZED **/
            deviceId = UUID.randomUUID().toString();
        }
        SDKContext.getInstance().setUpId(CommonMD5.getMD5(deviceId));
    }

    private String getGaid() {
        try {
            Class clz = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
            Class clzInfo = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient$Info");
            Method m = clz.getMethod("getAdvertisingIdInfo", Context.class);
            Object o = m.invoke(null, mContext);
//                                Class<? extends Object> infoClass = o.getClass();

            Method m2 = clzInfo.getMethod("getId");
            return (String) m2.invoke(o);

        } catch (Exception e) {
            // try to get from google play app library
            try {
                AdvertisingIdClient.AdInfo adInfo = new AdvertisingIdClient().getAdvertisingIdInfo(mContext);
                return adInfo.getId();

            } catch (Exception e1) {
            }
        }
        return "";
    }

    private boolean isGaidInvalid(String deviceId) {
//        deviceId = "00000000-0000-0000-0000-000000000000";//无效模拟数据
        return Pattern.matches("^[0-]+$", deviceId);
    }

    @Override
    protected Object onParseResponse(Map<String, List<String>> headers, String jsonString) throws IOException {
        jsonString = jsonString.trim();
        AgentEventManager.sentHostCallbackTime("app", null, startTime, System.currentTimeMillis());
        return jsonString;
    }

    @Override
    protected void handleSaveHttpRequest(AdError adError) {

    }

    @Override
    protected void onErrorAgent(String msg, AdError adError) {
        AgentEventManager.sendErrorAgent("app", adError.getPlatformCode(), adError.getPlatformMSG(), null, "", "");
    }


}
