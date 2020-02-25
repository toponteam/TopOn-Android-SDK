package com.anythink.network.tapjoy;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleSignatureVerifier;
import com.tapjoy.TJConnectListener;
import com.tapjoy.Tapjoy;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class TapjoyATInitManager extends ATInitMediation {
    private static final String TAG = TapjoyATInitManager.class.getSimpleName();
    private static TapjoyATInitManager sInstance;
    private String mSdkKey;

    private TapjoyATInitManager() {

    }

    public static TapjoyATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new TapjoyATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public void initSDK(Context context, Map<String, Object> serviceExtras, final TJConnectListener connectListener) {

        final String appkey = (String) serviceExtras.get("sdk_key");

        if (TextUtils.isEmpty(mSdkKey) || !TextUtils.equals(mSdkKey, appkey)) {

            try {
                if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
                    //Whether to agree to collect data
                    boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
                    //Whether to agree to collect data
                    boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");

                    if (need_set_gdpr) {
                        //1:agree  0:deny
                        String consent = gdp_consent ? "1" : "0";
                        Tapjoy.setUserConsent(consent);

                        Tapjoy.subjectToGDPR(ATSDK.isEUTraffic(context));
                    }
                }
                logGDPRSetting(TapjoyATConst.NETWORK_FIRM_ID);

                final Hashtable<String, Object> connectFlags = new Hashtable<String, Object>();
                connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, ATSDK.NETWORK_LOG_DEBUG);
                TapjoyLog.setDebugEnabled(ATSDK.NETWORK_LOG_DEBUG);

                Tapjoy.connect(context, appkey, connectFlags, new TJConnectListener() {
                    @Override
                    public void onConnectSuccess() {
                        mSdkKey = appkey;
                        if (connectListener != null) {
                            connectListener.onConnectSuccess();
                        }
                    }

                    @Override
                    public void onConnectFailure() {
                        if (connectListener != null) {
                            connectListener.onConnectFailure();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                if (connectListener != null) {
                    connectListener.onConnectFailure();
                }
            }
        } else {
            if (connectListener != null) {
                connectListener.onConnectSuccess();
            }
        }
    }

    @Override
    public String getNetworkName() {
        return "Tapjoy";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.tapjoy.Tapjoy";
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
        list.add("com.tapjoy.TJAdUnitActivity");
        list.add("com.tapjoy.TJContentActivity");
        return list;
    }
}
