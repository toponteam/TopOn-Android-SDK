/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.Window;

import com.anythink.core.common.base.Const;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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

    public static Map<String, Object> jsonObjectToMap(String jsonString) {
        final Map<String, Object> serviceExtras = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> serviceObject = jsonObject.keys();
            while (serviceObject.hasNext()) {
                String key = serviceObject.next();
                Object value = jsonObject.opt(key);
                serviceExtras.put(key, value);
            }
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return serviceExtras;
    }

    public static String[] jsonArrayToStringArray(JSONArray jsonArray) {
        try {
            String[] stringArray = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                stringArray[i] = jsonArray.optString(i);
            }
            return stringArray;
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void hideNavigationBar(final Activity activity) {

        final Window window = activity.getWindow();
        if (window != null) {
            final View view = window.getDecorView();
            hideNavigation(view);
            view.setOnSystemUiVisibilityChangeListener(createHideNavigationListener(view));
        }
    }

    static View.OnSystemUiVisibilityChangeListener createHideNavigationListener(
            final View view) {

        return new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    hideNavigation(view);
                }
            }
        };
    }

    static void hideNavigation(final View view) {

        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

}
