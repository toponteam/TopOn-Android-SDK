/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.nend;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.anythink.core.api.ATInitMediation;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleSignatureVerifier;

import java.util.HashMap;
import java.util.Map;

public class NendATInitManager extends ATInitMediation {

    public static final String TAG = NendATInitManager.class.getSimpleName();
    private static NendATInitManager sInstance;

    private NendATInitManager() {

    }

    public synchronized static NendATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new NendATInitManager();
        }
        return sInstance;
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {

    }

    @Override
    public String getNetworkName() {
        return "Nend";
    }

    @Override
    public String getNetworkSDKClass() {
        return "net.nend.android.internal.ui.activities.video.NendAdRewardedVideoActivity";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("play-services-ads-identifier-*.aar", false);
        pluginMap.put("play-services-basement-*.aar", false);
        pluginMap.put("constraintlayout-*.aar", false);


        Class clazz;
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

        try {
            clazz = ConstraintLayout.class;
            pluginMap.put("constraintlayout-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

}
