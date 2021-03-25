/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.flurry;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgentListener;
import com.flurry.android.FlurryBrowserActivity;
import com.flurry.android.FlurryConsent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.util.Log.VERBOSE;


public class FlurryATInitManager extends ATInitMediation {

    private static final String TAG = FlurryATInitManager.class.getSimpleName();
    private String mSDKKey;
    private static FlurryATInitManager sInstance;

    private Handler mHandler;

    private FlurryATInitManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public synchronized static FlurryATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new FlurryATInitManager();
        }
        return sInstance;
    }


    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        String sdkKey = (String) serviceExtras.get("sdk_key");


        if (!TextUtils.isEmpty(sdkKey)) {

            if (TextUtils.isEmpty(mSDKKey) || !mSDKKey.equals(sdkKey)) {

                FlurryAgent.Builder builder = new FlurryAgent.Builder();

                builder.withCaptureUncaughtExceptions(true)
                        .withContinueSessionMillis(10000)
                        .withLogLevel(VERBOSE)
                        .build(context.getApplicationContext(), sdkKey);
                mSDKKey = sdkKey;
            }
        }
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        Object[] result = new Object[2];

        String iabConsentString = "";
        Map<String, String> consentStrings = new HashMap<>();
        consentStrings.put("IAB", iabConsentString);
        result[0] = iabConsentString;
        result[1] = isConsent;
        FlurryConsent flurryConsent = new FlurryConsent(isConsent, consentStrings);

        FlurryAgent.updateFlurryConsent(flurryConsent);
        return true;
    }


    public void postDelay(Runnable runnable, long time) {
        mHandler.postDelayed(runnable, time);
    }

    @Override
    public String getNetworkName() {
        return "Flurry";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.flurry.android.FlurryAgent";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("flurryAdnlytics_*.aar", false);

        Class clazz;
        try {
            clazz = FlurryAgent.class;
            pluginMap.put("flurryAdnlytics_*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.flurry.android.FlurryFullscreenTakeoverActivity");
        list.add("com.flurry.android.FlurryTileAdActivity");
        list.add("com.flurry.android.FlurryBrowserActivity");
        return list;
    }
}
