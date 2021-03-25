/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.network.mytarget;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.anythink.core.api.ATInitMediation;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleSignatureVerifier;
import com.my.target.common.MyTargetPrivacy;

import java.util.HashMap;
import java.util.Map;

public class MyTargetATInitManager extends ATInitMediation {

    private static final String TAG = MyTargetATInitManager.class.getSimpleName();

    private static MyTargetATInitManager sInstance;


    private MyTargetATInitManager() {

    }

    public static synchronized MyTargetATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new MyTargetATInitManager();
        }
        return sInstance;
    }


    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {

    }


    @Override
    public String getNetworkName() {
        return "MyTarget";
    }

    @Override
    public String getNetworkVersion() {
        return MyTargetATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.my.target.common.MyTargetManager";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("recyclerview-*.aar", false);
        pluginMap.put("play-services-ads-identifier-*.aar", false);
        pluginMap.put("play-services-basement-*.aar", false);

        Class clazz;
        try {
            clazz = RecyclerView.class;
            pluginMap.put("recyclerview-*.aar", true);
        } catch (Throwable e) {
        }

        try {
            clazz = AdvertisingIdClient.class;
            pluginMap.put("play-services-ads-identifier-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = GoogleSignatureVerifier.class;
            pluginMap.put("play-services-basement-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }


    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        MyTargetPrivacy.setUserConsent(isConsent);
        return true;
    }
}
