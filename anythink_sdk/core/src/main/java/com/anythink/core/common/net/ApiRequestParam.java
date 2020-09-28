package com.anythink.core.common.net;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.IATChinaSDKHandler;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

import org.json.JSONObject;

public class ApiRequestParam {
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
    public static final String JSON_REQUEST_SYSTEM = "system"; //Brand


    public static final String JSON_REQUEST_ANDROID_ID = "android_id";
    public static final String JSON_REQUEST_COMMON_GAID = "gaid";
    public static final String JSON_REQUEST_COMMON_CHANNEL = "channel";
    public static final String JSON_REQUEST_COMMON_SUBCHANNEL = "sub_channel";
    public static final String JSON_REQUEST_COMMON_UPID = "upid";
    public static final String JSON_REQUEST_COMMON_PSID = "ps_id";
    public static final String JSON_REQUEST_INSTALL_SOURCE = "it_src";

    public static final String JSON_REQUEST_GDPR_LEVEL = "gdpr_cs";
    public static final String JSON_REQUEST_ABTEST_ID = "abtest_id";

    public static final String JSON_REQUEST_FIRST_INIT_TIME = "first_init_time";
    public static final String JSON_REQUEST_DAYS_FROM_FIRST_INIT = "days_from_first_init";


    public static final String JSON_REQUEST_APPID = "app_id";
    public static final String JSON_REQUEST_API_VERSION = "api_ver";

    /**
     * For Agent & TK
     **/
    public static final String JSON_REQUEST_DATA_LIST = "data";
    public static final String JSON_REQUEST_TKDA_REPORT_TYPE = "tcp_tk_da_type";
    public static final String JSON_REQUEST_TK_OFFLINE_TAG = "ofl";
    public static final String JSON_REQUEST_TCP_RATE = "tcp_rate";


    public static final String JSON_REQUEST_P = "p";
    public static final String JSON_REQUEST_P2 = "p2";
    public static final String JSON_REQUEST_SIGIN = "sign";

    public static final String JSON_REQUEST_COMMON = "common";


    public static final int POST = 1;
    public static final int GET = 2;
    public static final int TCP = 3;

    /**
     * Commmon Data
     *
     * @return
     */
    public static JSONObject getBaseInfoObject() {
        if (TextUtils.isEmpty(SDKContext.getInstance().getUpId())) {
            CommonDeviceUtil.initUpId(SDKContext.getInstance().getContext());
            AgentEventManager.sdkInitEvent("", "3", "", String.valueOf(System.currentTimeMillis()));
        }

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
            deviceJSONObject.put(JSON_REQUEST_COMMON_UPID, UploadDataLevelManager.getInstance(context).canUpLoadDeviceData() ? SDKContext.getInstance().getUpId() : "");

            deviceJSONObject.put(JSON_REQUEST_COMMON_PSID, SDKContext.getInstance().getPsid());

            //Set AbTest id
            AppStrategy appStrategy = AppStrategyManager.getInstance(context).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
            if (appStrategy != null) {
                deviceJSONObject.put(JSON_REQUEST_ABTEST_ID, TextUtils.isEmpty(appStrategy.getAbTestId()) ? "" : appStrategy.getAbTestId());
            }

            deviceJSONObject.put(JSON_REQUEST_FIRST_INIT_TIME, SDKContext.getInstance().getFirstInitTime());
            deviceJSONObject.put(JSON_REQUEST_DAYS_FROM_FIRST_INIT, SDKContext.getInstance().getInitDays());

        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return deviceJSONObject;
    }

    /**
     * Device Data
     *
     * @return
     */
    public static JSONObject getMainInfoObject() {
        Context context = SDKContext.getInstance().getContext();
        JSONObject mainObject = new JSONObject();
        AppStrategy appStrategy = AppStrategyManager.getInstance(context).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        try {
            boolean isUpLoadAndroidId = true;
            String dataLevel = appStrategy != null ? appStrategy.getDataLevel() : "";
            if (!TextUtils.isEmpty(dataLevel)) {
                try {
                    JSONObject jsonObject = new JSONObject(dataLevel);
                    if (!jsonObject.isNull("a")) {
                        isUpLoadAndroidId = jsonObject.optInt("a") == 1;
                    }
                } catch (Exception e) {

                }
            }
            mainObject.put(JSON_REQUEST_ANDROID_ID, isUpLoadAndroidId ? CommonDeviceUtil.getAndroidID(context) : "");
            mainObject.put(JSON_REQUEST_COMMON_GAID, CommonDeviceUtil.getGoogleAdId());
            IATChinaSDKHandler chinaSDKHandler = SDKContext.getInstance().getChinaHandler();
            if (chinaSDKHandler != null) {
                chinaSDKHandler.fillRequestData(mainObject, appStrategy);
                mainObject.put("is_cn_sdk", "1");
            } else {
                mainObject.put("is_cn_sdk", "0");
            }
            String installSourcePkg = CommonDeviceUtil.getSourceInstallPackageName(context);

            mainObject.put(JSON_REQUEST_INSTALL_SOURCE, !TextUtils.isEmpty(installSourcePkg) ? installSourcePkg : "");

        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return mainObject;
    }
}
