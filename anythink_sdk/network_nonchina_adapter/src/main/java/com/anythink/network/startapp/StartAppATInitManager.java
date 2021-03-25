/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.startapp;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StartAppATInitManager extends ATInitMediation {

    private static final String TAG = StartAppATInitManager.class.getSimpleName();
    private String mAppId;
    private static StartAppATInitManager sIntance;

    private StartAppATInitManager() {

    }

    public synchronized static StartAppATInitManager getInstance() {
        if (sIntance == null) {
            sIntance = new StartAppATInitManager();
        }
        return sIntance;
    }


    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        if (!(context instanceof Activity)) {
            return;
        }

        String appId = (String) serviceExtras.get("app_id");

        if (!TextUtils.isEmpty(appId)) {
            if (TextUtils.isEmpty(mAppId) || !TextUtils.equals(mAppId, appId)) {
                StartAppSDK.init(context.getApplicationContext(), appId, false);
                StartAppAd.disableAutoInterstitial();
                StartAppAd.disableSplash();
                mAppId = appId;
            }
        }
    }

    @Override
    public String getNetworkName() {
        return "StartApp";
    }

    @Override
    public String getNetworkVersion() {
        return StartAppATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.startapp.sdk.adsbase.StartAppSDK";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.startapp.sdk.adsbase.consent.ConsentActivity");
        list.add("com.startapp.sdk.ads.list3d.List3DActivity");
        list.add("com.startapp.sdk.adsbase.activities.OverlayActivity");
        list.add("com.startapp.sdk.adsbase.activities.FullScreenActivity");
        return list;
    }

    @Override
    public List getServiceStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.startapp.sdk.adsbase.InfoEventService");
        list.add("com.startapp.sdk.adsbase.PeriodicJobService");
        return list;
    }

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.startapp.sdk.adsbase.StartAppInitProvider");
        return list;
    }
}
