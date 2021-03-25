/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.tapjoy;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.anythink.core.common.base.Const;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleSignatureVerifier;
import com.tapjoy.TJConnectListener;
import com.tapjoy.TJPrivacyPolicy;
import com.tapjoy.Tapjoy;

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

    public synchronized static TapjoyATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new TapjoyATInitManager();
        }
        return sInstance;
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras, final TJConnectListener connectListener) {

        final String appkey = (String) serviceExtras.get("sdk_key");

        if (TextUtils.isEmpty(mSdkKey) || !TextUtils.equals(mSdkKey, appkey)) {

            try {
                final Hashtable<String, Object> connectFlags = new Hashtable<>();
//                connectFlags.put(TapjoyConnectFlag.ENABLE_LOGGING, ATSDK.isNetworkLogDebug());
//                TapjoyLog.setDebugEnabled(ATSDK.isNetworkLogDebug());
                try {
                    boolean ccpaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_CCPA_SWITCH_KEY);
                    if (ccpaSwitch) {
                        TJPrivacyPolicy privacyPolicy = TJPrivacyPolicy.getInstance();
                        privacyPolicy.setUSPrivacy("1YYY");
                    }
                } catch (Throwable e) {

                }

                try {
                    boolean coppaSwitch = (boolean) serviceExtras.get(Const.NETWORK_REQUEST_PARAMS_KEY.APP_COPPA_SWITCH_KEY);
                    if (coppaSwitch) {
                        TJPrivacyPolicy privacyPolicy = TJPrivacyPolicy.getInstance();
                        privacyPolicy.setBelowConsentAge(true);
                    }
                } catch (Throwable e) {

                }

                Tapjoy.connect(context.getApplicationContext(), appkey, connectFlags, new TJConnectListener() {
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
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        //1:agree  0:deny
        String consent = isConsent ? "1" : "0";
        TJPrivacyPolicy.getInstance().setUserConsent(consent);
        TJPrivacyPolicy.getInstance().setSubjectToGDPR(ATSDK.isEUTraffic(context));
        return true;
    }

    @Override
    public String getNetworkName() {
        return "Tapjoy";
    }

    @Override
    public String getNetworkVersion() {
        return TapjoyATConst.getNetworkVersion();
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
