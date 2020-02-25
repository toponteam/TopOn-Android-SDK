package com.anythink.network.facebook;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.anythink.core.api.ATInitMediation;
import com.facebook.ads.AudienceNetworkAds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacebookATInitManager extends ATInitMediation {

    private static final String TAG = FacebookATInitManager.class.getSimpleName();
    private boolean mIsInit;
    private static FacebookATInitManager sInstance;

    private FacebookATInitManager() {

    }

    public static FacebookATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new FacebookATInitManager();
        }
        return sInstance;
    }


    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        try {
            if (!mIsInit) {
                AudienceNetworkAds.initialize(context.getApplicationContext());
                mIsInit = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNetworkName() {
        return "Facebook";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.facebook.ads.AudienceNetworkAds";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("recyclerview-v7-*.aar", false);

        Class clazz;
        try {
            clazz = RecyclerView.class;
            pluginMap.put("recyclerview-v7-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.facebook.ads.AudienceNetworkActivity");
        list.add("com.facebook.ads.internal.ipc.RemoteANActivity");
        return list;
    }

    @Override
    public List getServiceStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.facebook.ads.internal.ipc.AdsProcessPriorityService");
        list.add("com.facebook.ads.internal.ipc.AdsMessengerService");
        return list;
    }

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.facebook.ads.AudienceNetworkContentProvider");
        return list;
    }
}
