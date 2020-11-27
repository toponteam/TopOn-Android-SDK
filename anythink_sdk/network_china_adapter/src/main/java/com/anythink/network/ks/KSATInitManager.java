/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ks;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.SdkConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KSATInitManager extends ATInitMediation {

    private static final String TAG = KSATInitManager.class.getSimpleName();
    private String mAppId;
    private static KSATInitManager sInstance;

    private Map<String, Object> adObject = new ConcurrentHashMap<>();

    private KSATInitManager() {

    }

    protected void put(String adsourceId, Object object) {
        adObject.put(adsourceId, object);
    }

    protected void remove(String adsourceId) {
        adObject.remove(adsourceId);
    }

    public synchronized static KSATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new KSATInitManager();
        }
        return sInstance;
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        String app_id = (String) serviceExtras.get("app_id");

        if (!TextUtils.isEmpty(app_id)) {
            if (TextUtils.isEmpty(mAppId) || !TextUtils.equals(mAppId, app_id)){
                KsAdSDK.init(context, new SdkConfig.Builder()
                        .appId(app_id)
//                        .debug(ATSDK.isNetworkLogDebug())
                        .build());

                mAppId = app_id;
            }
        }
    }

    @Override
    public String getNetworkName() {
        return "Kuaishou";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.kwad.sdk.api.KsAdSDK";
    }

}
