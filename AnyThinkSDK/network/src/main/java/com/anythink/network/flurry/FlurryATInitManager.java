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

/**
 * Created by Z on 2018/1/30.
 */

public class FlurryATInitManager extends ATInitMediation {

    private static final String TAG = FlurryATInitManager.class.getSimpleName();
    private String mSDKKey;
    private static FlurryATInitManager sInstance;

    private Handler mHandler;

    private FlurryATInitManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static FlurryATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new FlurryATInitManager();
        }
        return sInstance;
    }


    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        String sdkKey = (String) serviceExtras.get("sdk_key");


        if (!TextUtils.isEmpty(sdkKey)) {
            Object[] result = supportGDPR(context, serviceExtras);

            if (TextUtils.isEmpty(mSDKKey) || !mSDKKey.equals(sdkKey)) {

                FlurryAgent.Builder builder = new FlurryAgent.Builder();
                //防止无GDPR 版本出现报错
                try {
                    Map<String, String> consentStrings = new HashMap<>();
                    consentStrings.put("IAB", (String) result[0]);

                    FlurryConsent flurryConsent = new FlurryConsent((boolean) result[1], consentStrings);
                    builder.withConsent(flurryConsent)
                            .withListener(new FlurryAgentListener() {
                                @Override
                                public void onSessionStarted() {
                                    Log.d("flurry", "onSessionStarted....");
                                }
                            });
                } catch (Exception pE) {

                }

                builder.withLogEnabled(ATSDK.NETWORK_LOG_DEBUG)
                        .withCaptureUncaughtExceptions(true)
                        .withContinueSessionMillis(10000)
                        .withLogLevel(VERBOSE)
                        .build(context.getApplicationContext(), sdkKey);
                mSDKKey = sdkKey;
            }
        }
    }

    private Object[] supportGDPR(Context context, Map<String, Object> serviceExtras) {
        Object[] result = new Object[2];

        String iabConsentString = "";

        if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
            //Whether to agree to collect data
            boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
            //Whether to set the GDPR of the network
            boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");

            if (need_set_gdpr) {
                try {
                    Map<String, String> consentStrings = new HashMap<>();
                    consentStrings.put("IAB", iabConsentString);
                    result[0] = iabConsentString;
                    result[1] = gdp_consent;
                    FlurryConsent flurryConsent = new FlurryConsent(gdp_consent, consentStrings);

                    FlurryAgent.updateFlurryConsent(flurryConsent);
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    result[0] = "";
                    result[1] = false;
                }
            }
        }

        logGDPRSetting(FlurryATConst.NETWORK_FIRM_ID);

        return result;
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
        pluginMap.put("flurryAds_*.aar", false);

        Class clazz;
        try {
            clazz = FlurryBrowserActivity.class;
            pluginMap.put("flurryAds_*.aar", true);
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
