/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.mopub;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.anythink.core.api.ATInitMediation;
import com.google.android.gms.common.api.internal.BasePendingResult;
import com.google.gson.Gson;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.BuildConfig;
import com.mopub.mobileads.MoPubFullscreen;
import com.mopub.mobileads.MoPubInline;
import com.mopub.nativeads.NativeAd;
import com.mopub.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MopubATInitManager extends ATInitMediation {

    private static final String TAG = MopubATInitManager.class.getSimpleName();
    private static MopubATInitManager sInstance;

    private boolean mIsIniting;
    private final Object mLock = new Object();
    private List<InitListener> mListeners;

    private MopubATInitManager() {

    }

    public synchronized static MopubATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new MopubATInitManager();
        }
        return sInstance;
    }


    public synchronized void initSDK(final Context context, final Map<String, Object> serviceExtras, final InitListener initListener) {

        synchronized (mLock) {
            if (!MoPub.isSdkInitialized()) {

                if (mListeners == null) {
                    mListeners = new ArrayList<>();
                }

                if (initListener != null) {
                    mListeners.add(initListener);
                }

                if (mIsIniting) {
                    return;
                }

                mIsIniting = true;

                String unitid = (String) serviceExtras.get("unitid");

                SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(unitid).build();
                MoPub.initializeSdk(context, sdkConfiguration, new SdkInitializationListener() {
                    @Override
                    public void onInitializationFinished() {
                        callbackSuccess();
                    }
                });

            } else {
                if (initListener != null) {
                    initListener.initSuccess();
                }
            }
        }
    }

    private void callbackSuccess() {
        synchronized (mLock) {
            int size = mListeners.size();
            InitListener initListener;
            for (int i = 0; i < size; i++) {
                initListener = mListeners.get(i);
                if (initListener != null) {
                    initListener.initSuccess();
                }
            }
            mListeners.clear();
        }
    }


    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        PersonalInfoManager mPersonalInfoManager = MoPub.getPersonalInformationManager();
        if (mPersonalInfoManager == null) {
            return false;
        }
        if (isConsent) {
            //Agree
            mPersonalInfoManager.grantConsent();
        } else {
            //Refuse
            mPersonalInfoManager.revokeConsent();
        }
        return true;
    }

    interface InitListener {
        void initSuccess();
    }

    @Override
    public String getNetworkName() {
        return "Mopub";
    }

    @Override
    public String getNetworkVersion() {
        return MopubATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.mopub.common.MoPub";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("mopub-sdk-*.aar", false);
        pluginMap.put("mopub-sdk-banner-*.aar", false);
        pluginMap.put("mopub-sdk-fullscreen-*.aar", false);
        pluginMap.put("mopub-sdk-native-static-*.aar", false);
        pluginMap.put("mopub-volley-*.aar", false);

        pluginMap.put("recyclerview-*.aar", false);
        pluginMap.put("gson-*.jar", false);
        pluginMap.put("play-services-base-*.aar", false);

        Class clazz;
        try {
            clazz = BuildConfig.class;
            pluginMap.put("mopub-sdk-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MoPubInline.class;
            pluginMap.put("mopub-sdk-banner-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MoPubFullscreen.class;
            pluginMap.put("mopub-sdk-fullscreen-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = NativeAd.class;
            pluginMap.put("mopub-sdk-native-static-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = Volley.class;
            pluginMap.put("mopub-volley-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = RecyclerView.class;
            pluginMap.put("recyclerview-*.aar", true);
        } catch (Throwable e) {
        }

        try {
            clazz = Gson.class;
            pluginMap.put("gson-*.jar", true);
        } catch (Throwable e) {
        }

        try {
            clazz = BasePendingResult.class;
            pluginMap.put("play-services-base-*.aar", true);
        } catch (Throwable e) {
        }

        return pluginMap;
    }

}
