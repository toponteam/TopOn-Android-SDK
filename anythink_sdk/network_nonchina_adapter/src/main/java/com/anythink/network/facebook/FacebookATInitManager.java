package com.anythink.network.facebook;

import android.content.Context;

import com.anythink.core.api.ATInitMediation;
import com.facebook.ads.AudienceNetworkAds;

import java.util.HashMap;
import java.util.Map;

public class FacebookATInitManager extends ATInitMediation {

    private static final String TAG = FacebookATInitManager.class.getSimpleName();
    private boolean mIsInit;
    private static FacebookATInitManager sInstance;

    private FacebookATInitManager() {

    }

    public synchronized static FacebookATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new FacebookATInitManager();
        }
        return sInstance;
    }


    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
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
        pluginMap.put("recyclerview-*.aar", false);

        Class clazz;
        try {
            clazz = Class.forName("android.support.v7.widget.RecyclerView");
            pluginMap.put("recyclerview-*.aar", true);
        } catch (Throwable e) {
        }

        return pluginMap;
    }

}
