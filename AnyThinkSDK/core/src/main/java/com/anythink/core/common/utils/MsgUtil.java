package com.anythink.core.common.utils;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MsgUtil {

    public static final String JSON_REQUEST_COMMON_PLATFORM = "platform";
    public static final String JSON_REQUEST_COMMON_QS_VERSION_NAME = "os_vn";
    public static final String JSON_REQUEST_COMMON_OS_VERSION_CODE = "os_vc";
    public static final String JSON_REQUEST_COMMON_APP_PACKAGE_NAME = "package_name";
    public static final String JSON_REQUEST_COMMON_APP_VERSION_NAME = "app_vn";
    public static final String JSON_REQUEST_COMMON_APP_VERSION_CODE = "app_vc";
    public static final String JSON_REQUEST_COMMON_BRAND = "brand";
    public static final String JSON_REQUEST_COMMON_MODEL = "model";
    public static final String JSON_REQUEST_COMMON_SCREEN_SIZE = "screen";
    //统一网络就叫network_type
    public static final String JSON_REQUEST_COMMON_NETWORK_TYPE = "network_type";
    public static final String JSON_REQUEST_COMMON_MNC = "mnc";
    public static final String JSON_REQUEST_COMMON_MCC = "mcc";
    public static final String JSON_REQUEST_COMMON_LANGUAGE = "language";
    public static final String JSON_REQUEST_COMMON_TIMEZONE = "timezone";
    public static final String JSON_REQUEST_COMMON_SDKVERSION = "sdk_ver";
    public static final String JSON_REQUEST_COMMON_GP_VERSION = "gp_ver";
    public static final String JSON_REQUEST_COMMON_NW_VERSION = "nw_ver";
    public static final String JSON_REQUEST_COMMON_UA = "ua";
    public static final String JSON_REQUEST_ORIENTATION = "orient";
    public static final String JSON_REQUEST_SYSTEM = "system";


    public static final String JSON_REQUEST_ANDROID_ID = "android_id";
    public static final String JSON_REQUEST_COMMON_GAID = "gaid";
    public static final String JSON_REQUEST_COMMON_CHANNEL = "channel";
    public static final String JSON_REQUEST_COMMON_SUBCHANNEL = "sub_channel";
    public static final String JSON_REQUEST_COMMON_UPID = "upid";
    public static final String JSON_REQUEST_COMMON_PSID = "ps_id";


    protected static JSONObject getBaseInfoObject() {
        JSONObject deviceJSONObject = new JSONObject();
        Context context = SDKContext.getInstance().getContext();
        try {
            deviceJSONObject.put(JSON_REQUEST_COMMON_PLATFORM, 1);
            deviceJSONObject.put(JSON_REQUEST_COMMON_QS_VERSION_NAME, CommonDeviceUtil.getOSversionName());
            deviceJSONObject.put(JSON_REQUEST_COMMON_OS_VERSION_CODE, CommonDeviceUtil.getOsVersion());
            deviceJSONObject.put(JSON_REQUEST_COMMON_APP_PACKAGE_NAME, CommonDeviceUtil.getPackageName(context));
            deviceJSONObject.put(JSON_REQUEST_COMMON_APP_VERSION_NAME, CommonDeviceUtil.getVersionName(context));
            deviceJSONObject.put(JSON_REQUEST_COMMON_APP_VERSION_CODE, CommonDeviceUtil.getVersionCode(context) + "");
            deviceJSONObject.put(JSON_REQUEST_COMMON_BRAND, CommonDeviceUtil.getPhoneBrand());
            deviceJSONObject.put(JSON_REQUEST_COMMON_MODEL, CommonDeviceUtil.getModel());
            deviceJSONObject.put(JSON_REQUEST_COMMON_SCREEN_SIZE, CommonDeviceUtil.getScreenSize(context));
            deviceJSONObject.put(JSON_REQUEST_COMMON_NETWORK_TYPE, String.valueOf(CommonDeviceUtil.getNetworkType(context)));
            deviceJSONObject.put(JSON_REQUEST_COMMON_MNC, CommonDeviceUtil.getMNC(context));
            deviceJSONObject.put(JSON_REQUEST_COMMON_MCC, CommonDeviceUtil.getMCC(context));
            deviceJSONObject.put(JSON_REQUEST_COMMON_LANGUAGE, CommonDeviceUtil.getLanguage(context));
            deviceJSONObject.put(JSON_REQUEST_COMMON_TIMEZONE, CommonDeviceUtil.getTimeZone());
            deviceJSONObject.put(JSON_REQUEST_COMMON_SDKVERSION, Const.SDK_VERSION_NAME);
            deviceJSONObject.put(JSON_REQUEST_COMMON_GP_VERSION, CommonDeviceUtil.getGoogleVersion(context));
            deviceJSONObject.put(JSON_REQUEST_COMMON_UA, CommonDeviceUtil.getDefaultUA());
            deviceJSONObject.put(JSON_REQUEST_ORIENTATION, CommonDeviceUtil.orientation(context));
            deviceJSONObject.put(JSON_REQUEST_SYSTEM, Const.SYSTEM);
            if (!TextUtils.isEmpty(SDKContext.getInstance().getChannel())) {
                deviceJSONObject.put(JSON_REQUEST_COMMON_CHANNEL, SDKContext.getInstance().getChannel());
            }
            if (!TextUtils.isEmpty(SDKContext.getInstance().getSubChannel())) {
                deviceJSONObject.put(JSON_REQUEST_COMMON_SUBCHANNEL, SDKContext.getInstance().getSubChannel());
            }
            deviceJSONObject.put(JSON_REQUEST_COMMON_UPID, SDKContext.getInstance().getUpId());
            deviceJSONObject.put(JSON_REQUEST_COMMON_PSID, SDKContext.getInstance().getPsid());
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return deviceJSONObject;
    }

    protected static JSONObject getMainInfoObject() {
        Context context = SDKContext.getInstance().getContext();
        JSONObject mainObject = new JSONObject();
        try {
            mainObject.put(JSON_REQUEST_ANDROID_ID, CommonDeviceUtil.getAndroidID(context));
            mainObject.put(JSON_REQUEST_COMMON_GAID, CommonDeviceUtil.getGoogleAdId());

        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return mainObject;
    }

    public static JSONObject getCommonObject() {
        JSONObject commonObject = getBaseInfoObject();
        JSONObject mainObject = getMainInfoObject();

        try {
            commonObject.put("app_id", SDKContext.getInstance().getAppId());
            Iterator<String> iterator = mainObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                commonObject.put(key, mainObject.opt(key));
            }
        } catch (JSONException e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return commonObject;
    }


}
