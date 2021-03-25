/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.facebook;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.common.base.Const;
import com.facebook.ads.AdSettings;
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
                try {
                    boolean ccpaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_CCPA_SWITCH_KEY);
                    if (ccpaSwitch) {
                        AdSettings.setDataProcessingOptions(new String[] {"LDU"}, 1, 1000);
                    }
                } catch (Throwable e) {

                }

                try {
                    boolean coppaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_COPPA_SWITCH_KEY);
                    if (coppaSwitch) {
                        AdSettings.setMixedAudience(true);
                    }
                } catch (Throwable e) {

                }
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
    public String getNetworkVersion() {
        return FacebookATConst.getNetworkVersion();
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
            clazz = RecyclerView.class;
            pluginMap.put("recyclerview-*.aar", true);
        } catch (Throwable e) {
        }

        return pluginMap;
    }

}
