/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.oneway;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.didi.virtualapk.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mobi.oneway.export.Ad.OnewaySdk;

public class OnewayATInitManager extends ATInitMediation {

    private static final String TAG = OnewayATInitManager.class.getSimpleName();
    private String mPublisherId;

    private static OnewayATInitManager sIntance;

    private OnewayATInitManager() {

    }

    public synchronized static OnewayATInitManager getInstance() {
        if (sIntance == null) {
            sIntance = new OnewayATInitManager();
        }
        return sIntance;
    }


    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {

        final String publishId = (String) serviceExtras.get("publisher_id");

        if (TextUtils.isEmpty(mPublisherId) || !TextUtils.equals(mPublisherId, publishId)) {
            OnewaySdk.configure(context, publishId);
//            OnewaySdk.setDebugMode(ATSDK.isNetworkLogDebug());
            mPublisherId = publishId;
        }
    }

    @Override
    public String getNetworkName() {
        return "Oneway";
    }

    @Override
    public String getNetworkSDKClass() {
        return "mobi.oneway.export.Ad.OnewaySdk";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("core-*.aar", false);
        pluginMap.put("virtualapk-core-*.aar", false);

        Class clazz;
        try {
            clazz = Class.forName("mobi.oneway.common.provider.OwCommonFileProvider");
            pluginMap.put("core-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = PluginManager.class;
            pluginMap.put("virtualapk-core-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("mobi.oneway.export.AdShowActivity");
        return list;
    }


    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("mobi.oneway.export.OWProvider");
        return list;
    }
}
