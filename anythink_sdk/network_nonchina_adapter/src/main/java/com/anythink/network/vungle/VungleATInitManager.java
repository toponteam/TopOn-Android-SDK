package com.anythink.network.vungle;

import android.content.Context;

import com.anythink.core.api.ATInitMediation;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleSignatureVerifier;
import com.google.gson.Gson;
import com.moat.analytics.mobile.vng.MoatAdEvent;
import com.vungle.warren.InitCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.error.VungleException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Okio;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VungleATInitManager extends ATInitMediation {

    private static final String TAG = VungleATInitManager.class.getSimpleName();
    private static VungleATInitManager sInstance;

    private boolean mIsIniting;
    private List<InitListener> mListeners;
    private final Object mLock = new Object();

    private VungleATInitManager() {

    }

    public synchronized static VungleATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new VungleATInitManager();
        }
        return sInstance;
    }

    protected synchronized void initSDK(Context context, Map<String, Object> serviceExtras, final InitListener listener) {
        synchronized (mLock) {
            final String appId = serviceExtras.get("app_id").toString();

            if (mListeners == null) {
                mListeners = new ArrayList<>();
            }

            if (!Vungle.isInitialized()) {
                if (listener != null) {
                    mListeners.add(listener);
                }

                if (mIsIniting) {
                    return;
                }

                mIsIniting = true;

                try {
                    if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
                        //GDPR Consent
                        boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
                        //Need to set GDPR
                        boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");

                        if (need_set_gdpr) {
                            Vungle.updateConsentStatus(gdp_consent ? Vungle.Consent.OPTED_IN : Vungle.Consent.OPTED_OUT, "1.0.0");
                        }
                    }

                    logGDPRSetting(VungleATConst.NETWORK_FIRM_ID);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                Vungle.init(appId, context, new InitCallback() {
                    @Override
                    public void onSuccess() {
                        callbackResult(true, null);
                    }

                    @Override
                    public void onError(VungleException throwable) {
                        callbackResult(false, throwable);
                    }

                    @Override
                    public void onAutoCacheAdAvailable(String s) {

                    }
                });
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        }
    }

    private void callbackResult(boolean success, VungleException throwable) {
        synchronized (mLock) {
            int size = mListeners.size();
            InitListener initListener;
            for (int i = 0; i < size; i++) {
                initListener = mListeners.get(i);
                if (initListener != null) {
                    if (success) {
                        initListener.onSuccess();
                    } else {
                        initListener.onError(throwable);
                    }
                }
            }
            mListeners.clear();
        }
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public interface InitListener {
        void onSuccess();

        void onError(Throwable throwable);
    }

    @Override
    public String getNetworkName() {
        return "Vungle";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.vungle.warren.Vungle";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("play-services-ads-identifier-*.aar", false);
        pluginMap.put("play-services-basement-*.aar", false);

        pluginMap.put("converter-gson-*.aar", false);
        pluginMap.put("gson-*.aar", false);
        pluginMap.put("logging-interceptor-*.aar", false);
        pluginMap.put("okhttp-*.jar", false);
        pluginMap.put("okio-*.jar", false);
        pluginMap.put("retrofit-*.jar", false);
        pluginMap.put("vng-moat-mobile-app-kit-*.jar", false);


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

        try {
            clazz = GsonConverterFactory.class;
            pluginMap.put("converter-gson-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = Gson.class;
            pluginMap.put("gson-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = HttpLoggingInterceptor.Logger.class;
            pluginMap.put("logging-interceptor-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = OkHttpClient.class;
            pluginMap.put("okhttp-*.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = Okio.class;
            pluginMap.put("okio-*.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = Retrofit.class;
            pluginMap.put("retrofit-*.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MoatAdEvent.class;
            pluginMap.put("vng-moat-mobile-app-kit-*.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.vungle.warren.ui.VungleActivity");
        list.add("com.vungle.warren.ui.VungleFlexViewActivity");
//        list.add("com.vungle.warren.ui.VungleWebViewActivity");
        return list;
    }
}
