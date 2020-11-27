/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.anythink.core.common.base.AdvertisingIdClient;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;
import com.anythink.core.common.net.ApiRequestParam;
import com.anythink.core.common.track.AgentEventManager;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class CommonDeviceUtil {

    private static String mSelfId;
    private static String adid;

    public static String networkVersionStr;

    public static String sourceInstallPackageName;

    private CommonDeviceUtil() {
    }

    /**
     * Init Device info
     *
     * @param context
     */
    public static void initCommonDeviceInfo(Context context) {
        try {
            CommonDeviceUtil.getOsVersion();
            CommonDeviceUtil.getPackageName(context);
            CommonDeviceUtil.getVersionName(context);
            CommonDeviceUtil.getVersionCode(context);
            CommonDeviceUtil.orientation(context);
            CommonDeviceUtil.getModel();
            CommonDeviceUtil.getPhoneBrand();
            CommonDeviceUtil.getAndroidID(context);
            CommonDeviceUtil.getGoogleAdId();
            CommonDeviceUtil.getLanguage(context);
            CommonDeviceUtil.getTimeZone();
            CommonDeviceUtil.getGoogleVersion(context);
            CommonDeviceUtil.initNetworkVersionJSON(context);
            if (CommonUtil.isGranted("android.permission.READ_PHONE_STATE", context)) {
                TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String simOperator = TelephonyMgr.getSimOperator();
                if (CommonUtil.isNotNullOrEmpty(simOperator) && simOperator.length() > 3) {
                    mccString = simOperator.substring(0, 3);
                    mncString = simOperator.substring(3, simOperator.length());
                }
            }
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }


    }


    /**
     * 获取手机国家号码
     *
     * @return
     */
    public static String getMCC(Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_MCC)) {
            return "";
        }


        try {
            //如果上报等级不为PERSONALIZED，则不会传入该字段信息
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            //如果抛出异常证明sdk还没初始化，无法判断上报等级
            return "";
        }
        return mccString;
    }

    /**
     * 获取手机运营商代码
     *
     * @return
     */
    public static String getMNC(Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_MNC)) {
            return "";
        }


        try {
            //如果上报等级不为PERSONALIZED，则不会传入该字段信息
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            //如果抛出异常证明sdk还没初始化，无法判断上报等级
            return "";
        }
        return mncString;
    }

    /**
     * Android ID
     *
     * @return
     */
    private static String androidId_click;

    public static String getAndroidID(Context context) {

        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_ANDROID_ID)) {
            return "";
        }

        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
        try {
            if (TextUtils.isEmpty(androidId_click)) {

                androidId_click = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

                if (androidId_click == null) {
                    androidId_click = "";
                }
            }
        } catch (Exception e) {
            androidId_click = "";
        }

        return androidId_click;
    }

    /**
     * Phone model
     *
     * @return
     */
    public static String getModel() {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_MODEL)) {
            return "";
        }


        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
        return Build.MODEL;
    }


    /**
     * Phone Brand
     *
     * @return
     */
    public static String getPhoneBrand() {

        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_BRAND)) {
            return "";
        }

        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
        return Build.BRAND;
    }

    /**
     * System language
     *
     * @return
     */
    public static String getLanguage(Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_LANGUAGE)) {
            return "";
        }


        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
        if (TextUtils.isEmpty(language)) {
            Locale locale = context.getResources().getConfiguration().locale;
            language = locale.getLanguage();
            return language;
        }
        return language;
    }


    /**
     * Application's orientation
     *
     * @return
     */
    public static int orientation(Context c) {

        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_ORIENTATION)) {
            return 0;
        }

        Configuration cf = c.getResources().getConfiguration();

        int ori = cf.orientation;

        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            return 2;
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            return 1;
        }

        return 1;
    }


    /**
     * app version code
     *
     * @return
     */
    public static String getVersionCode(Context context) {

        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_APP_VERSION_CODE)) {
            return "";
        }

        if (versionCode == 0) {
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                versionCode = pi.versionCode;
                return versionCode + "";
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        return versionCode + "";
    }

    /**
     * app version name
     *
     * @return
     */
    public static String getVersionName(Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_APP_VERSION_NAME)) {
            return "";
        }

        try {
            if (TextUtils.isEmpty(versionName)) {
                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                versionName = pi.versionName;
                return versionName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return versionName;

    }

    public static int getDisplayW(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getDisplayH(Context context) {

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static String getScreenSize(Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_SCREEN_SIZE)) {
            return "";
        }


        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
        return getDisplayW(context) + "*" + getDisplayH(context);
    }

    /**
     * @return
     */
    public static String getPackageName(Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_APP_PACKAGE_NAME)) {
            return "";
        }

        try {
            if (TextUtils.isEmpty(packageName)) {
                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                packageName = pi.packageName;
                return packageName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return packageName;

    }

    /**
     * Get install package name
     *
     * @param context
     * @return
     */
    public static String getSourceInstallPackageName(Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_INSTALL_SOURCE)) {
            return "";
        }

        try {
            if (TextUtils.isEmpty(sourceInstallPackageName)) {
                sourceInstallPackageName = context.getPackageManager().getInstallerPackageName(getPackageName(context));
                return sourceInstallPackageName;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return sourceInstallPackageName;
    }


    private static String userAgent = "";
    private static String osversionname = "";
    private static String osversion = "";
    private static String packageName = "";
    private static String versionName = "";
    private static int versionCode = 0;
    private static String language = "";
    private static String timeZone = "";
    private static String gPVersion = "";
    private static String mccString = "";
    private static String mncString = "";

    /**
     * @param context
     * @return
     */
    public static int getNetwork(Context context) {
        int netType = Const.NET_TYPE_UNKNOW;
        try {
            if (context == null) {
                return Const.NET_TYPE_UNKNOW;
            }

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return Const.NET_TYPE_UNKNOW;
            }
            if (CommonUtil.isGranted("android.permission.ACCESS_NETWORK_STATE", context)) {

                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo == null) {
                    return Const.NET_TYPE_UNKNOW;
                }
                if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return Const.NET_TYPE_WIFI;
                }
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm == null) {
                    return Const.NET_TYPE_UNKNOW;
                }
                netType = tm.getNetworkType();
                return netType;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Const.NET_TYPE_UNKNOW;
        }
        return netType;
    }

    /**
     * @param context
     * @return
     */
    public static String getNetworkType(Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_NETWORK_TYPE)) {
            return "";
        }

        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }

        int netType = Const.NET_TYPE_UNKNOW;
        try {
            if (context == null) {
                return Const.NET_TYPE_UNKNOW + "";
            }

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return Const.NET_TYPE_UNKNOW + "";
            }
            if (CommonUtil.isGranted("android.permission.ACCESS_NETWORK_STATE", context)) {

                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo == null) {
                    return Const.NET_TYPE_UNKNOW + "";
                }
                if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return Const.NET_TYPE_WIFI + "";
                }
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm == null) {
                    return Const.NET_TYPE_UNKNOW + "";
                }
                netType = tm.getNetworkType();
                return getNetworkClass(netType) + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Const.NET_TYPE_UNKNOW + "";
        }
        return netType + "";
    }


    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return Const.NET_TYPE_2G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return Const.NET_TYPE_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return Const.NET_TYPE_4G;
            default:
                return Const.NET_TYPE_UNKNOW;
        }
    }


    public static String getTimeZone() {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_TIMEZONE)) {
            return "";
        }


        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
        try {
            if (TextUtils.isEmpty(timeZone)) {
                TimeZone tz = TimeZone.getDefault();
                timeZone = tz.getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH);
                return timeZone;
            }
        } catch (Throwable e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

        return timeZone;
    }

    /**
     * OS Version
     *
     * @return
     */
    public static String getOsVersion() {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_OS_VERSION_CODE)) {
            return "";
        }

        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }

        if (TextUtils.isEmpty(osversion)) {
            int osversion_int = getOsVersionInt();
            osversion = osversion_int + "";
        }

        return osversion;
    }

    public static String getOSversionName() {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_QS_VERSION_NAME)) {
            return "";
        }

        if (TextUtils.isEmpty(osversionname)) {
            osversionname = Build.VERSION.RELEASE;
        }
        return osversionname;
    }

    /**
     * OS Version Code
     *
     * @return
     */
    public static int getOsVersionInt() {
        return Build.VERSION.SDK_INT;

    }

    public static void setGoogleAdId(String ad) {
        adid = ad;
        SPUtil.putString(SDKContext.getInstance().getContext(), Const.SPU_NAME, Const.SPU_SYS_GAID, adid);

    }

    /**
     * Gaid
     *
     * @return
     */
    public static String getGoogleAdId() {
        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }

        if (TextUtils.isEmpty(adid)) {
            adid = SPUtil.getString(SDKContext.getInstance().getContext(), Const.SPU_NAME, Const.SPU_SYS_GAID, "");
        }
        return adid;

    }

    /**
     * @return
     */
    public static String getGoogleVersion(Context context) {
        if (TextUtils.isEmpty(gPVersion)) {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo info = pm.getPackageInfo(CommonSDKUtil.AppStoreUtils.PACKAGE_NAME_GOOGLE_PLAY, 0);
                gPVersion = info.versionName;
                return gPVersion;
            } catch (Exception e) {
                return "";
            }
        }
        return gPVersion;
    }

    private static void initNetworkVersionJSON(Context context) {
        networkVersionStr = SPUtil.getString(context, Const.SPU_NAME, Const.SPUKEY.SPU_NETWORK_VERSION_NAME, "");
    }

    /**
     * All Mediation Version
     *
     * @return
     */
    public static JSONObject getAllNetworkVersion() {
        if (!TextUtils.isEmpty(networkVersionStr)) {
            try {
                JSONObject jsonObject = new JSONObject(networkVersionStr);
                return jsonObject;
            } catch (Exception e) {

            }
        }
        return new JSONObject();
    }


    public synchronized static void putNetworkSDKVersion(int networkType, String version) {
        if (!TextUtils.isEmpty(networkVersionStr)) {
            try {
                JSONObject jsonObject = new JSONObject(networkVersionStr);
                if (jsonObject.has(String.valueOf(networkType))) {
                    return;
                } else {
                    jsonObject.put(String.valueOf(networkType), version);
                    networkVersionStr = jsonObject.toString();
                }
            } catch (Exception e) {

            }
        } else {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(String.valueOf(networkType), version);
                networkVersionStr = jsonObject.toString();
            } catch (Exception e) {

            }
        }
    }

    public static synchronized String getDefaultUA() {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_UA)) {
            return "";
        }

        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return "";
            }
        } catch (Exception e) {
            return "";
        }

        if (!TextUtils.isEmpty(userAgent)) {
            return userAgent;
        }
        String version = Build.VERSION.RELEASE;
        String phoneModel = getModel();
        String buildId = Build.ID;
        if (!TextUtils.isEmpty(version) && !TextUtils.isEmpty(phoneModel) && !TextUtils.isEmpty(buildId)) {
            return "Mozilla/5.0 (Linux; Android " + version + "; " + phoneModel + " Build/" + buildId
                    + ") AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";
        } else {
            return "";
        }
    }

    /**
     * User Agent
     *
     * @return
     */
    public static void getDefaultUserAgent_UI(final Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_UA)) {
            return;
        }

        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        userAgent = SPUtil.getString(context, Const.SPU_NAME, Const.SPU_LOCAL_USERAGENT, "");
        String os_version = SPUtil.getString(context, Const.SPU_NAME, Const.SPU_LOCAL_OS, "");

        if (!TextUtils.isEmpty(userAgent) && Build.VERSION.RELEASE.equals(os_version)) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (TextUtils.isEmpty(userAgent)) {
                return;
            }
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                userAgent = WebSettings.getDefaultUserAgent(context);
            } else {
                userAgent = new WebView(context).getSettings().getUserAgentString();
            }
            //更新一下UA
            SPUtil.putString(context, Const.SPU_NAME, Const.SPU_LOCAL_USERAGENT, userAgent);
            SPUtil.putString(context, Const.SPU_NAME, Const.SPU_LOCAL_OS, Build.VERSION.RELEASE);
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }

        } catch (Throwable e) {
        }

        if (TextUtils.isEmpty(userAgent)) {
            return;
        }

    }

    /**
     * init upid
     */
    public static synchronized void initUpId(Context context) {
        if (!TextUtils.isEmpty(SDKContext.getInstance().getUpId())) {
            return;
        }
        String deviceId = "";
        deviceId = getGaid(context);
        if (TextUtils.isEmpty(deviceId) || isGaidInvalid(deviceId)) {
            deviceId = getAndroidID(context);
        }
        if (TextUtils.isEmpty(deviceId)) {
            /**if deviceid is null，create the uuid**/
            deviceId = UUID.randomUUID().toString();
        }
        SDKContext.getInstance().setUpId(CommonMD5.getMD5(deviceId));

        //init upid agent
        AgentEventManager.sdkInitEvent("", "3", "", String.valueOf(System.currentTimeMillis()));
    }


    /**
     * Must run on other thread
     *
     * @param context
     * @return
     */
    private static String getGaid(final Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ApiRequestParam.JSON_REQUEST_COMMON_GAID)) {
            return "";
        }

        if (SDKContext.getInstance().getChinaHandler() != null) {
            return "";
        }

        final ExecutorService service = Executors.newFixedThreadPool(2);
        final String[] gaid = new String[1];

        service.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Class clz = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
                    Class clzInfo = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient$Info");
                    Method m = clz.getMethod("getAdvertisingIdInfo", Context.class);
                    Object o = m.invoke(null, context);
//                                Class<? extends Object> infoClass = o.getClass();

                    Method m2 = clzInfo.getMethod("getId");
                    gaid[0] = (String) m2.invoke(o);

                } catch (Exception e) {
                    // try to get from google play app library
                    try {
                        AdvertisingIdClient.AdInfo adInfo = new AdvertisingIdClient().getAdvertisingIdInfo(context);
                        gaid[0] = adInfo.getId();

                    } catch (Exception e1) {
                    }
                }

                if (!TextUtils.isEmpty(gaid[0]) && !isGaidInvalid(gaid[0])) {
                    CommonDeviceUtil.setGoogleAdId(gaid[0]);
                }

                try {
                    synchronized (service) {
                        service.notifyAll();
                    }
                } catch (Throwable e) {

                }

            }
        });


        try {
            synchronized (service) {
                service.wait(2000);
            }
            service.shutdown();
            return gaid[0] != null ? gaid[0] : "";
        } catch (Exception e) {

        }


        return "";
    }

    private static boolean isGaidInvalid(String deviceId) {
//        deviceId = "00000000-0000-0000-0000-000000000000";//无效模拟数据
        return Pattern.matches("^[0-]+$", deviceId);
    }


}
