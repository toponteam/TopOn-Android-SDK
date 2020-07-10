package com.anythink.china.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 工程里关于imei和meid获取不正确。避免修改后影响工程特单独写一份。
 * 或许二次开发使用本工具类
 */
public class ImeiUtils {


    /**
     * 4.0
     */
    public static String getImeiOrMeid(Context ctx) {
        TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        try {
            if (manager != null) {
                return manager.getDeviceId();
            }
        } catch (Throwable e) {

        }
        return null;
    }

    /**
     * 5.0
     *
     * @param ctx
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static Map getImeiAndMeid(Context ctx) {
        Map<String, String> map = new HashMap<String, String>();
        TelephonyManager mTelephonyManager = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        Class<?> clazz = null;
        Method method = null;//(int slotId)

        try {
            clazz = Class.forName("android.os.SystemProperties");
            method = clazz.getMethod("get", String.class, String.class);
            String gsm = (String) method.invoke(null, "ril.gsm.imei", "");
            String meid = (String) method.invoke(null, "ril.cdma.meid", "");
            map.put("meid", meid);
            if (!TextUtils.isEmpty(gsm)) {
                //the value of gsm like:xxxxxx,xxxxxx
                String imeiArray[] = gsm.split(",");
                if (imeiArray != null && imeiArray.length > 0) {
                    map.put("imei1", imeiArray[0]);

                    if (imeiArray.length > 1) {
                        map.put("imei2", imeiArray[1]);
                    } else {
                        map.put("imei2", mTelephonyManager.getDeviceId(1));
                    }
                } else {
                    map.put("imei1", mTelephonyManager.getDeviceId(0));
                    map.put("imei2", mTelephonyManager.getDeviceId(1));
                }
            } else {
                map.put("imei1", mTelephonyManager.getDeviceId(0));
                map.put("imei2", mTelephonyManager.getDeviceId(1));

            }

        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (Throwable e) {

        }
        return map;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static Map getIMEIforO(Context context) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            TelephonyManager tm = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
            String imei1 = tm.getImei(0);
            String imei2 = tm.getImei(1);
            if (TextUtils.isEmpty(imei1) && TextUtils.isEmpty(imei2)) {

                map.put("imei1", tm.getMeid());
            } else {
                map.put("imei1", imei1);

                map.put("imei2", imei2);
            }
        } catch (Throwable e) {

        }


        return map;
    }


    public static String getIMEI(Context ctx) {

        String imei = "";
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                imei = getImeiOrMeid(ctx);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Map imeiMaps = getImeiAndMeid(ctx);
                imei = getTransform(imeiMaps);
            } else {
                Map imeiMaps = getIMEIforO(ctx);

                imei = getTransform(imeiMaps);
            }
        } catch (Throwable e) {

        }


        return imei;
    }


    private static String getTransform(Map imeiMaps) {
        String imei = "";
        if (imeiMaps != null) {
            String imei1 = (String) imeiMaps.get("imei1");
            imei = imei1;
        }
        return imei;
    }
}