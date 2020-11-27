/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.gdt;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.androidquery.AQuery;
import com.anythink.core.api.ATInitMediation;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.comm.managers.GDTADManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GDTATInitManager extends ATInitMediation {

    public static final String TAG = GDTATInitManager.class.getSimpleName();

    private static GDTATInitManager sInstance;

    private Map<String, Object> adObject = new ConcurrentHashMap<>();

    private GDTATInitManager() {

    }

    public synchronized static GDTATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new GDTATInitManager();
        }
        return sInstance;
    }

    protected void put(String adsourceId, Object object) {
        adObject.put(adsourceId, object);
    }

    protected void remove(String adsourceId) {
        adObject.remove(adsourceId);
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras, OnInitCallback onInitCallback) {

        boolean success;
        if (!GDTADManager.getInstance().isInitialized()) {
            String app_id = (String) serviceExtras.get("app_id");

            success = GDTADManager.getInstance().initWith(context.getApplicationContext(), app_id);
        } else {
            success = true;
        }

        if (onInitCallback != null) {
            if (success) {
                onInitCallback.onSuccess();
            } else {
                onInitCallback.onError();
            }
        }
    }

    @Override
    public String getNetworkName() {
        return "Tencent";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.qq.e.ads.ADActivity";
    }


    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();

        pluginMap.put("android-query-full.*.aar", false);

        Class clazz;
        try {
            clazz = AQuery.class;
            pluginMap.put("android-query-full.*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }


    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.qq.e.ads.ADActivity");
        list.add("com.qq.e.ads.PortraitADActivity");
        list.add("com.qq.e.ads.LandscapeADActivity");
        list.add("com.qq.e.ads.RewardvideoPortraitADActivity");
        list.add("com.qq.e.ads.RewardvideoLandscapeADActivity");
        return list;
    }

    @Override
    public List getServiceStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.qq.e.comm.DownloadService");
        return list;
    }

    public interface OnInitCallback {
        void onSuccess();

        void onError();
    }

    protected int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / (scale <= 0 ? 1 : scale) + 0.5f);
    }

     int getVideoPlayPolicy(Context context, int autoPlayPolicy) {
        if (autoPlayPolicy == VideoOption.AutoPlayPolicy.ALWAYS) {
            return VideoOption.VideoPlayPolicy.AUTO;
        } else if (autoPlayPolicy == VideoOption.AutoPlayPolicy.WIFI) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return wifiNetworkInfo != null && wifiNetworkInfo.isConnected() ? VideoOption.VideoPlayPolicy.AUTO
                        : VideoOption.VideoPlayPolicy.MANUAL;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (autoPlayPolicy == VideoOption.AutoPlayPolicy.NEVER) {
            return VideoOption.VideoPlayPolicy.MANUAL;
        }
        return VideoOption.VideoPlayPolicy.UNKNOWN;
    }

}
