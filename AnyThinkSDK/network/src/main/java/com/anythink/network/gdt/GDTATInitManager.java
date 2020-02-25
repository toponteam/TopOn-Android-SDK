package com.anythink.network.gdt;

import android.content.Context;

import com.androidquery.AQuery;
import com.anythink.core.api.ATInitMediation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GDTATInitManager extends ATInitMediation {

    public static final String TAG = GDTATInitManager.class.getSimpleName();

    private static GDTATInitManager sInstance;

    private GDTATInitManager() {

    }

    public static GDTATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new GDTATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {

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
}
