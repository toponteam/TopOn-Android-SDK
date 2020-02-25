package com.anythink.network.applovin;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.applovin.sdk.AppLovinPrivacySettings;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplovinATInitManager extends ATInitMediation {

    private static final String TAG = ApplovinATInitManager.class.getSimpleName();
    private static ApplovinATInitManager sInstance;

    private String mSdkKey;

    private ApplovinATInitManager() {

    }

    public static ApplovinATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new ApplovinATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        String sdkkey = (String) serviceExtras.get("sdkkey");

        if (!TextUtils.isEmpty(sdkkey)) {
            initSDK(context, sdkkey, serviceExtras);
        }
    }

    public AppLovinSdk initSDK(Context context, String sdkKey, Map<String, Object> serviceExtras) {

        if (TextUtils.isEmpty(mSdkKey) || !TextUtils.equals(mSdkKey, sdkKey)) {
            superGDPR(context, serviceExtras);
            mSdkKey = sdkKey;
        }
        AppLovinSdk appLovinSdk = AppLovinSdk.getInstance(sdkKey, new AppLovinSdkSettings(), context);
        appLovinSdk.getSettings().setVerboseLogging(ATSDK.NETWORK_LOG_DEBUG);
        return appLovinSdk;
    }

    /***
     * Whether to support GDPR
     */
    private void superGDPR(Context context, Map<String, Object> serviceExtras) {
        if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
            //Whether to agree to collect data
            boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
            //Whether to set the GDPR of the network
            boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");

            if (need_set_gdpr) {
                AppLovinPrivacySettings.setHasUserConsent(gdp_consent, context);
            }
        }

        logGDPRSetting(ApplovinATConst.NETWORK_FIRM_ID);
    }

    @Override
    public String getNetworkName() {
        return "Applovin";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.applovin.sdk.AppLovinSdk";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.applovin.adview.AppLovinInterstitialActivity");
        list.add("com.applovin.sdk.AppLovinWebViewActivity");
        list.add("com.applovin.mediation.MaxDebuggerActivity");
        list.add("com.applovin.mediation.MaxDebuggerDetailActivity");
        return list;
    }

    @Override
    public List getServiceStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.applovin.impl.sdk.utils.AppKilledService");
        return list;
    }
}
