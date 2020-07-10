package com.anythink.network.uniplay;

import android.content.Context;

import com.anythink.core.api.ATInitMediation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UniplayATInitManager extends ATInitMediation {

    public static final String TAG = UniplayATInitManager.class.getSimpleName();

    private static UniplayATInitManager sInstance;

    private UniplayATInitManager() {

    }

    public synchronized static UniplayATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new UniplayATInitManager();
        }
        return sInstance;
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {

    }

    @Override
    public String getNetworkName() {
        return "Uniplay";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.uniplay.adsdk.VideoAd";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.uniplay.adsdk.AdActivity");
        list.add("com.uniplay.adsdk.InterstitialAdActivity");
        list.add("com.uniplay.adsdk.NetworkChangeActivity");
        list.add("com.joomob.activity.AdVideoActivity");
        return list;
    }

    @Override
    public List getServiceStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.uniplay.adsdk.DownloadService");
        return list;
    }

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.uniplay.adsdk.UniPlayFileProvider");
        return list;
    }
}
