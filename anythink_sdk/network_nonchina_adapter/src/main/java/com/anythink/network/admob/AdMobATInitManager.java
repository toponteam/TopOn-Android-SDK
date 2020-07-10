package com.anythink.network.admob;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.internal.ClientApi;
import com.google.android.gms.common.GoogleSignatureVerifier;
import com.google.android.gms.internal.ads.zzdpt;
import com.google.android.gms.internal.measurement.zzl;
import com.google.android.gms.measurement.api.AppMeasurementSdk;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.internal.ads.zzdou;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/30.
 */

public class AdMobATInitManager extends ATInitMediation {

    private static final String TAG = AdMobATInitManager.class.getSimpleName();
    private String mAppId;
    private static AdMobATInitManager sInstance;

    private Map<String, Object> mOfferMap;

    private AdMobATInitManager() {

    }

    public synchronized static AdMobATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new AdMobATInitManager();
        }
        return sInstance;
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        String app_id = (String) serviceExtras.get("app_id");

        if (!TextUtils.isEmpty(app_id)) {
            if (TextUtils.isEmpty(mAppId) || !mAppId.equals(app_id)) {
                suportGDPR(context, serviceExtras);
                MobileAds.initialize(context, app_id);
                mAppId = app_id;
            }
        }
    }

    /***
     * GDPR
     */
    private void suportGDPR(Context context, Map<String, Object> serviceExtras) {

        if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
            //Whether to agree to collect data
            boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
            //Whether to set the GDPR of the network
            boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");

            if (need_set_gdpr) {
                ConsentInformation.getInstance(context)
                        .setConsentStatus(gdp_consent ? ConsentStatus.PERSONALIZED : ConsentStatus.NON_PERSONALIZED);
            }
        }

        logGDPRSetting(AdmobATConst.NETWORK_FIRM_ID);


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
            clazz = GoogleSignatureVerifier.class;
            pluginMap.put("play-services-basement-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = zzdou.class;
            pluginMap.put("play-services-gass-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = zzl.class;
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
}
