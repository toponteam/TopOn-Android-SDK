/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.inmobi;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.browser.customtabs.CustomTabsService;
import androidx.recyclerview.widget.RecyclerView;

import com.anythink.core.api.ATInitMediation;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleSignatureVerifier;
import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.SdkInitializationListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import okio.Okio;


public class InmobiATInitManager extends ATInitMediation {

    private static final String TAG = InmobiATInitManager.class.getSimpleName();
    private String mAccountId;
    private static InmobiATInitManager sInstance;

    private Handler mHandler;
    List<Object> inmobiAdObjects;

    ConcurrentHashMap<String, Object> mBidAdObject = new ConcurrentHashMap<>();

    private InmobiATInitManager() {
        mHandler = new Handler(Looper.getMainLooper());
        inmobiAdObjects = Collections.synchronizedList(new ArrayList<Object>());
    }

    public synchronized static InmobiATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new InmobiATInitManager();
        }
        return sInstance;
    }

    public void addInmobiAd(Object inmobiAd) {
        if (inmobiAd != null) {
            inmobiAdObjects.add(inmobiAd);
        }

    }

    public void removeInmobiAd(Object inmobiAd) {
        if (inmobiAd != null) {
            inmobiAdObjects.remove(inmobiAd);
        }
    }

    protected void putBidAdObject(String bidId, Object adObject) {
        mBidAdObject.put(bidId, adObject);
    }

    protected void removeBidAdObject(String bidId) {
        mBidAdObject.remove(bidId);
    }

    protected Object getBidAdObject(String bidId) {
        return mBidAdObject.get(bidId);
    }


    public void postDelay(Runnable runnable, long time) {
        mHandler.postDelayed(runnable, time);
    }

    public void post(Runnable runnable) {
        mHandler.post(runnable);
    }


    @Override
    public synchronized void initSDK(final Context context, final Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public synchronized void initSDK(final Context context, final Map<String, Object> serviceExtras, final OnInitCallback callback) {
        final String accountId = (String) serviceExtras.get("app_id");

        post(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(accountId)) {
                    //Must be executed by the main thread
                    try {
                        if (accountId.equals(mAccountId)) {
                            if (callback != null) {
                                callback.onSuccess();
                            }
                            return;
                        }

                        InMobiSdk.init(context.getApplicationContext(), accountId, jsonObject, new SdkInitializationListener() {
                            @Override
                            public void onInitializationComplete(Error error) {

                                if (callback != null) {
                                    if (error == null) {
                                        mAccountId = accountId;
                                        callback.onSuccess();
                                    } else {
                                        callback.onError(error.getMessage());
                                    }
                                }
                            }
                        });

                    } catch (Throwable e) {
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    }
                }
            }
        });

    }

    JSONObject jsonObject = new JSONObject();

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {

        try {
            //Is in EU?
            String gdprScope = isEUTraffic ? "1" : "0";
            // Provide correct consent value to sdk which is obtained by User

            jsonObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, isConsent);

            // Provide 0 if GDPR is not applicable and 1 if applicable
            jsonObject.put("gdpr", gdprScope);
            InMobiSdk.updateGDPRConsent(jsonObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public interface OnInitCallback {
        void onSuccess();

        void onError(String errorMsg);
    }

    @Override
    public String getNetworkName() {
        return "Inmobi";
    }

    @Override
    public String getNetworkVersion() {
        return InmobiATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.inmobi.sdk.InMobiSdk";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("okhttp-*.jar", false);
        pluginMap.put("okio-*.jar", false);
        pluginMap.put("picasso-*.aar", false);

        pluginMap.put("play-services-ads-identifier-*.aar", false);
        pluginMap.put("play-services-basement-*.aar", false);
        pluginMap.put("recyclerview-*.aar", false);
        pluginMap.put("support-customtabs-*.aar or androidx.browser.*.aar", false);

        Class clazz;
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
            clazz = Picasso.class;
            pluginMap.put("picasso-*.aar", true);
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
            clazz = RecyclerView.class;
            pluginMap.put("recyclerview-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = CustomTabsService.class;
            pluginMap.put("support-customtabs-*.aar or androidx.browser.*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.inmobi.ads.rendering.InMobiAdActivity");
        return list;
    }

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.squareup.picasso.PicassoProvider");
        return list;
    }
}
