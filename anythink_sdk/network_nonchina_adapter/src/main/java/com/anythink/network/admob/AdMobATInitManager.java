/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.admob;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.common.base.Const;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.internal.ClientApi;
import com.google.android.gms.common.util.PlatformVersion;
import com.google.android.gms.internal.ads.zzb;
import com.google.android.gms.internal.ads.zzdpt;
import com.google.android.gms.internal.measurement.zzfd;
import com.google.android.gms.measurement.api.AppMeasurementSdk;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdMobATInitManager extends ATInitMediation {

    private static final String TAG = AdMobATInitManager.class.getSimpleName();
    private String mAppId;

    private boolean mIsInit;
    private boolean mIsIniting;
    private static AdMobATInitManager sInstance;

    private Map<String, Object> mOfferMap;
    private final Object mLockObject = new Object();
    private List<InitListener> mInitListeners;

    private Handler mHandler;

    private boolean ccpaSwitch;
    private boolean coppaSwitch;

    private AdMobATInitManager() {
        mHandler = new Handler(Looper.getMainLooper());
        mIsInit = false;
    }

    public synchronized static AdMobATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new AdMobATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public void initSDK(final Context context, final Map<String, Object> serviceExtras, InitListener initListener) {
        synchronized (mLockObject) {
            if (mIsInit) {
                if (initListener != null) {
                    initListener.initSuccess();
                }
                return;
            }

            if (mInitListeners == null) {
                mInitListeners = new ArrayList<>();
            }
            mInitListeners.add(initListener);

            if (mIsIniting) {
                return;
            }
            mIsIniting = true;
        }

        try {
            ccpaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_CCPA_SWITCH_KEY);
        } catch (Throwable e) {

        }

        try {
            coppaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_COPPA_SWITCH_KEY);
            RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration();
            if (requestConfiguration == null) {
                requestConfiguration = new RequestConfiguration.Builder().build();
            }
            if (coppaSwitch) {
                requestConfiguration = requestConfiguration
                        .toBuilder()
                        .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                        .build();
                MobileAds.setRequestConfiguration(requestConfiguration);
            }
        } catch (Throwable e) {

        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String app_id = (String) serviceExtras.get("app_id");
                mAppId = app_id;
                MobileAds.initialize(context);
                mIsInit = true;
                mIsIniting = false;

                int size = mInitListeners.size();
                InitListener listener;
                for (int i = 0; i < size; i++) {
                    listener = mInitListeners.get(i);

                    if (listener != null) {
                        listener.initSuccess();
                    }
                }
                mInitListeners.clear();
            }
        });
    }

    /***
     * GDPR
     */
    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        try {
            ConsentInformation.getInstance(context)
                    .setConsentStatus(isConsent ? ConsentStatus.PERSONALIZED : ConsentStatus.NON_PERSONALIZED);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    public Bundle getRequestBundle(Context context) {
        Bundle bundle = new Bundle();
        bundle.putString("npa", "0");

        ConsentStatus consentStatus = ConsentInformation.getInstance(context).getConsentStatus();
        switch (consentStatus) {
            case UNKNOWN:
                break;
            case PERSONALIZED:
                break;
            case NON_PERSONALIZED:
                bundle.putString("npa", "1");
                break;
        }

        if (ccpaSwitch) {
            bundle.putString("rdp", "1");
        }
        return bundle;
    }

    public synchronized void addCache(String unitId, Object obj) {
        if (mOfferMap == null) {
            mOfferMap = new HashMap<>();
        }

        mOfferMap.put(unitId, obj);
    }

    public synchronized void removeCache(String unitId) {
        if (mOfferMap != null) {
            mOfferMap.remove(unitId);
        }
    }

    @Override
    public String getNetworkName() {
        return "Admob";
    }

    @Override
    public String getNetworkVersion() {
        return AdmobATConst.getNetworkVersion();
    }

    public String getGoogleAdManagerName() {
        return "Google Ad Manager";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.google.android.gms.ads.MobileAds";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("conscent-*.aar", false);

        pluginMap.put("play-services-ads-*.aar", false);
        pluginMap.put("play-services-ads-base-*.aar", false);
        pluginMap.put("play-services-ads-lite-*.aar", false);
        pluginMap.put("play-services-ads-identifier-*.aar", false);
        pluginMap.put("play-services-basement-*.aar", false);
        pluginMap.put("play-services-gass-*.aar", false);
        pluginMap.put("play-services-measurement-base-*.aar", false);
        pluginMap.put("play-services-measurement-sdk-api-*.aar", false);
        pluginMap.put("play=services-tasks-*.aar", false);

        Class clazz;
        try {
            clazz = ConsentInformation.class;
            pluginMap.put("conscent-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = Task.class;
            pluginMap.put("play=services-tasks-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = ClientApi.class;
            pluginMap.put("play-services-ads-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = zzdpt.class;
            pluginMap.put("play-services-ads-base-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = AdActivity.class;
            pluginMap.put("play-services-ads-lite-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = AdvertisingIdClient.class;
            pluginMap.put("play-services-ads-identifier-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = PlatformVersion.class;
            pluginMap.put("play-services-basement-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = zzb.class;
            pluginMap.put("play-services-gass-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = zzfd.class;
            pluginMap.put("play-services-measurement-base-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = AppMeasurementSdk.class;
            pluginMap.put("play-services-measurement-sdk-api-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getMetaValutStatus() {
        List<String> list = new ArrayList<>();
        list.add("com.google.android.gms.ads.APPLICATION_ID");
        return list;
    }

    interface InitListener {
        void initSuccess();
    }

}
