package com.anythink.network.adcolony;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleSignatureVerifier;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdColonyATInitManager extends ATInitMediation {

    private static final String TAG = AdColonyATInitManager.class.getSimpleName();
    String mAppId;
    String mZoneId;
    String[] mZoneIds;

    public static AdColonyATInitManager getInstance() {
        return Holder.sInstance;
    }

    private AdColonyATInitManager() {
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        String app_id = serviceExtras.get("app_id").toString();
        String zone_id = serviceExtras.get("zone_id").toString();
        String zoneIds = serviceExtras.get("zone_ids").toString();


        String[] zoneIdsArray = null;
        try {
            JSONArray jsonArray = new JSONArray(zoneIds);
            zoneIdsArray = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                zoneIdsArray[i] = jsonArray.optString(i);
            }
        } catch (Exception e) {

        }


        if (!TextUtils.isEmpty(app_id)) {
            if (zoneIdsArray != null && zoneIdsArray.length > 0) {
                initSDK(context, app_id, zoneIdsArray, serviceExtras);
            } else if (!TextUtils.isEmpty(zoneIds)) {
                initSDK(context, app_id, zone_id, serviceExtras);
            }
        }
    }

    static class Holder {
        static final AdColonyATInitManager sInstance = new AdColonyATInitManager();
    }

    private void initSDK(Context context, String app_id, String[] zone_ids, Map<String, Object> serviceExtras) {
        if (!checkIsNeedInit(app_id, zone_ids)) {
            return;
        }
        AdColonyAppOptions app_options = new AdColonyAppOptions();

        suportGDPR(context, app_options, serviceExtras);

        Application application = (Application) context.getApplicationContext();

        if (AdColony.configure(application, app_options, app_id, zone_ids)) {
            mAppId = app_id;
            mZoneIds = zone_ids;
        }
    }

    private void initSDK(Context context, String app_id, String zone_id, Map<String, Object> serviceExtras) {
        if (!TextUtils.isEmpty(mAppId) && !TextUtils.isEmpty(mZoneId)) {
            return;
        }
        AdColonyAppOptions app_options = new AdColonyAppOptions();
        suportGDPR(context, app_options, serviceExtras);

        Application application = (Application) context.getApplicationContext();

        if (AdColony.configure(application, app_options, app_id, zone_id)) {
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

    private void suportGDPR(Context activity, AdColonyAppOptions app_options, Map<String, Object> serviceExtras) {

        if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
            //Whether to agree to collect data
            boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
            //Whether to set the GDPR of the network
            boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");

            if (need_set_gdpr) {
                if (gdp_consent) {
                    app_options.setGDPRConsentString("1");
                } else {
                    app_options.setGDPRConsentString("0");
                }

                boolean isEUTraffic = ATSDK.isEUTraffic(activity);
                app_options.setGDPRRequired(isEUTraffic);
            }
        }

        logGDPRSetting(AdColonyATConst.NETWORK_FIRM_ID);

    }

    @Override
    public String getNetworkName() {
        return "AdColony";
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
