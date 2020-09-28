package com.anythink.network.ogury;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;

import java.util.Map;

import io.presage.Presage;

public class OguryATInitManager extends ATInitMediation {

    private String mAssetKey;
    private static OguryATInitManager sInstance;

    private OguryATInitManager() {

    }

    public synchronized static OguryATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new OguryATInitManager();
        }
        return sInstance;
    }

    public synchronized void initSDK(final Context context, Map<String, Object> serviceExtras, final Callback callback) {

        String assetkey = (String) serviceExtras.get("key");
        if (TextUtils.isEmpty(mAssetKey) || !TextUtils.equals(mAssetKey, assetkey)) {
//            supportGdpr(context, serviceExtras);
            Presage.getInstance().start(assetkey, context.getApplicationContext());
            mAssetKey = assetkey;
        }
        if (callback != null) {
            callback.onSuccess();
        }
    }

    protected void supportGdpr(Context context, Map<String, Object> serviceExtras) {

//        if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
//            //Whether to agree to collect data
//            boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
//            //Whether to set the GDPR of the network
//            boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");
//
//            if (need_set_gdpr) {
//                //set GDPR for Ogury(waiting for Ogury 's api)
//
//            }
//        }
//
//        logGDPRSetting(OguryATConst.NETWORK_FIRM_ID);
    }

    protected static String getErrorMsg(int code) {
        /*
            code 0: load failed
            code 1: phone not connected to internet.
            code 2: ad disabled
            code 3: various error (configuration file not synced)
            code 4: ad expires in 4 hours if it was not shown
            code 5: start method not called
        */
        switch (code) {
            case 0:
            default:
                return "load failed";
            case 1:
                return "phone not connected to internet";
            case 2:
                return "ad disabled";
            case 3:
                return "various error (configuration file not synced)";
            case 4:
                return "ad expires in 4 hours if it was not shown";
            case 5:
                return "start method not called";

        }
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public interface Callback {
        void onSuccess();
//        void onError(String msg);
    }

    @Override
    public String getNetworkName() {
        return "Ogury";
    }

    @Override
    public String getNetworkSDKClass() {
        return "io.presage.Presage";
    }
}
