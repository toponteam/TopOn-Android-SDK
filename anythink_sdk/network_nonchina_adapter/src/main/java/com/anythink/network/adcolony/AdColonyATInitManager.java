/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.adcolony;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyReward;
import com.adcolony.sdk.AdColonyRewardListener;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.common.base.Const;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleSignatureVerifier;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdColonyATInitManager extends ATInitMediation {

    private static final String TAG = AdColonyATInitManager.class.getSimpleName();
    String mAppId;
    String mZoneId;
    String[] mZoneIds;

    ConcurrentHashMap<String, AdColonyRewardListener> mRewardListeners;

    public synchronized static AdColonyATInitManager getInstance() {
        return Holder.sInstance;
    }

    private AdColonyATInitManager() {
        mRewardListeners = new ConcurrentHashMap<>();
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        String app_id = serviceExtras.get("app_id").toString();
        String zone_id = serviceExtras.get("zone_id").toString();
        String zoneIds = serviceExtras.get("zone_ids").toString();

        boolean ccpaSwitch = false;
        boolean coppaSwitch = false;


        String[] zoneIdsArray = null;
        try {
            JSONArray jsonArray = new JSONArray(zoneIds);
            zoneIdsArray = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                zoneIdsArray[i] = jsonArray.optString(i);
            }
        } catch (Exception e) {

        }

        AdColonyAppOptions appOptions = AdColony.getAppOptions();
        if (appOptions == null) {
            appOptions = new AdColonyAppOptions();
        }
        try {
            ccpaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_CCPA_SWITCH_KEY);
            if (ccpaSwitch) {
                appOptions.setPrivacyFrameworkRequired(AdColonyAppOptions.CCPA, true);
            }
            AdColony.setAppOptions(appOptions);
        } catch (Throwable e) {

        }

        try {
            coppaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_COPPA_SWITCH_KEY);
            if (coppaSwitch) {
                appOptions.setPrivacyFrameworkRequired(AdColonyAppOptions.COPPA, true);
            }
            AdColony.setAppOptions(appOptions);
        } catch (Throwable e) {

        }


        if (!TextUtils.isEmpty(app_id)) {
            if (zoneIdsArray != null && zoneIdsArray.length > 0) {
                initSDK(context, app_id, zoneIdsArray, serviceExtras);
            } else if (!TextUtils.isEmpty(zoneIds)) {
                initSDK(context, app_id, zone_id, serviceExtras);
            }
        }
    }

    protected void addRewardListener(String zoneId, AdColonyRewardListener rewardListener) {
        try {
            mRewardListeners.put(zoneId, rewardListener);
        } catch (Throwable e) {

        }
    }

    static class Holder {
        static final AdColonyATInitManager sInstance = new AdColonyATInitManager();
    }

    private void initSDK(Context context, String app_id, String[] zone_ids, Map<String, Object> serviceExtras) {
        if (!checkIsNeedInit(app_id, zone_ids)) {
            return;
        }

        Application application = (Application) context.getApplicationContext();

        if (AdColony.configure(application, AdColony.getAppOptions(), app_id, zone_ids)) {
            mAppId = app_id;
            mZoneIds = zone_ids;
        }

        AdColony.setRewardListener(new AdColonyRewardListener() {
            @Override
            public void onReward(AdColonyReward adColonyReward) {
                if (adColonyReward != null && adColonyReward.getZoneID() != null) {
                    AdColonyRewardListener rewardListener = mRewardListeners.get(adColonyReward.getZoneID());
                    if (rewardListener != null) {
                        rewardListener.onReward(adColonyReward);
                    }
                    mRewardListeners.remove(adColonyReward.getZoneID());
                }
            }
        });
    }

    private void initSDK(Context context, String app_id, String zone_id, Map<String, Object> serviceExtras) {
        if (!TextUtils.isEmpty(mAppId) && !TextUtils.isEmpty(mZoneId)) {
            return;
        }

        Application application = (Application) context.getApplicationContext();

        if (AdColony.configure(application, AdColony.getAppOptions(), app_id, zone_id)) {
            mAppId = app_id;
            mZoneId = zone_id;
        }
    }

    private boolean checkIsNeedInit(String app_id, String[] zone_ids) {
        try {
            if (mAppId == null) {
                return true;
            } else if (mZoneIds == null) {
                return true;
            }

            if (!TextUtils.equals(mAppId, app_id)) {
                return true;
            }

            int size = mZoneIds.length;
            if (size != zone_ids.length) {
                return true;
            }
            for (int i = 0; i < size; i++) {
                if (!TextUtils.equals(mZoneIds[i], zone_ids[i])) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {

        AdColonyAppOptions adColonyAppOptions = AdColony.getAppOptions();
        if (adColonyAppOptions == null) {
            adColonyAppOptions = new AdColonyAppOptions();
        }
        if (isConsent) {
            adColonyAppOptions.setPrivacyConsentString(AdColonyAppOptions.GDPR, "1");
        } else {
            adColonyAppOptions.setPrivacyConsentString(AdColonyAppOptions.GDPR, "0");
        }

        adColonyAppOptions.setPrivacyFrameworkRequired(AdColonyAppOptions.GDPR, isEUTraffic);
        AdColony.setAppOptions(adColonyAppOptions);
        return true;
    }

    @Override
    public String getNetworkName() {
        return "AdColony";
    }

    @Override
    public String getNetworkVersion() {
        return AdColonyATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.adcolony.sdk.AdColony";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("play-services-ads-identifier-*.aar", false);
        pluginMap.put("play-services-basement-*.aar", false);
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

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.adcolony.sdk.AdColonyInterstitialActivity");
        list.add("com.adcolony.sdk.AdColonyAdViewActivity");
        return list;
    }

}
