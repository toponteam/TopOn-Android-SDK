package com.anythink.core.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.UploadDataLevelManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

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
    public static int getVersionCode(Context context) {
        if (versionCode == 0) {
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                versionCode = pi.versionCode;
                return versionCode;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return versionCode;
    }

    /**
     * app version name
     *
     * @return
     */
    public static String getVersionName(Context context) {

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
     * @param context
     * @return
     */
    public static String getSourceInstallPackageName(Context context) {
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
    public static int getNetworkType(Context context) {
        try {
            if (!UploadDataLevelManager.getInstance(SDKContext.getInstance().getContext()).canUpLoadDeviceData()) {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }
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
                return getNetworkClass(netType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Const.NET_TYPE_UNKNOW;
        }
        return netType;
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
     * @return
     */
    public static String getOsVersion() {
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
     * @return
     */
    public static synchronized void getDefaultUserAgent_UI(final Context context) {
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

}
