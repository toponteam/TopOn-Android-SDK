package com.anythink.network.gdt;

import android.content.Context;

import com.androidquery.AQuery;
import com.anythink.core.api.ATInitMediation;
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

        pluginMap.put("ndroid-query-full.*.aar", false);

        Class clazz;
        try {
            clazz = AQuery.class;
            pluginMap.put("ndroid-query-full.*.aar", true);
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

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.anythink.network.gdt.GDTATFileProvider");
        return list;
    }

    public interface OnInitCallback {
        void onSuccess();

        void onError();
    }

}
