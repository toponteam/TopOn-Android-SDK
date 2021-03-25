/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.applovin;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.common.base.Const;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;

import java.util.Map;

public class ApplovinATInitManager extends ATInitMediation {

    private static final String TAG = ApplovinATInitManager.class.getSimpleName();
    private static ApplovinATInitManager sInstance;

    private String mSdkKey;

    private ApplovinATInitManager() {

    }

    public synchronized static ApplovinATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new ApplovinATInitManager();
        }
        return sInstance;
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        String sdkkey = (String) serviceExtras.get("sdkkey");

        if (!TextUtils.isEmpty(sdkkey)) {
            initSDK(context, sdkkey, serviceExtras);
        }
    }

    public AppLovinSdk initSDK(Context context, String sdkKey, Map<String, Object> serviceExtras) {

        if (TextUtils.isEmpty(mSdkKey) || !TextUtils.equals(mSdkKey, sdkKey)) {
            mSdkKey = sdkKey;
        }
        AppLovinSdk appLovinSdk = AppLovinSdk.getInstance(sdkKey, new AppLovinSdkSettings(), context);
        try {
            boolean coppaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_COPPA_SWITCH_KEY);
            if (coppaSwitch) {
                AppLovinPrivacySettings.setIsAgeRestrictedUser(true, context);
            }
        } catch (Throwable e) {

        }

//        appLovinSdk.getSettings().setVerboseLogging(ATSDK.isNetworkLogDebug());
        return appLovinSdk;
    }

    /***
     * Whether to support GDPR
     */
    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        AppLovinPrivacySettings.setHasUserConsent(isConsent, context);
        return true;
    }

    @Override
    public String getNetworkName() {
        return "Applovin";
    }

    @Override
    public String getNetworkVersion() {
        return ApplovinATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.applovin.sdk.AppLovinSdk";
    }
}
