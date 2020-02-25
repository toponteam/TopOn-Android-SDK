package com.anythink.core.common.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.anythink.core.common.base.Const;


/**
 * Util
 *
 * @author Z
 */
public class CommonUtil {

    public final static String TAG = "CommonUtils";

    public static <T extends String> boolean isNullOrEmpty(T t) {
        return t == null || t.length() == 0;
    }

    public static <T extends String> boolean isNotNullOrEmpty(T t) {
        return t != null && t.length() > 0;
    }


    /**
     * @param context
     * @return
     */
    public static boolean isNetConnect(Context context) {
        try {
            ConnectivityManager connectivitymanager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkinfo = connectivitymanager.getActiveNetworkInfo();
            return networkinfo != null && networkinfo.isAvailable();
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * dip to px
     *
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }


    public static boolean isGranted(String permission, Context context) {
        boolean result = false;

        try {
            PackageManager pm = context.getPackageManager();
            int hasPerm = pm.checkPermission(permission, context.getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                result = true;
                CommonLogUtil.d(TAG, "Permission " + permission + " is granted");
            } else {
                CommonLogUtil.d(TAG, "Permission " + permission + " is NOT granted");
            }
        } catch (Exception e) {
            return false;
        }
        return result;
    }

    /**
     * Get Anythink Resource-id
     *
     * @param context
     * @param resName
     * @param resType
     * @return
     */
    public static int getResId(Context context, String resName, String resType) {
        if (context != null) {
            resName = Const.RESOURCE_HEAD + "_" + resName;
            return context.getResources().getIdentifier(resName, resType,
                    context.getPackageName());
        }
        return -1;
    }


}
