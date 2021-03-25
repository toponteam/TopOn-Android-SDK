/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.network.kidoz;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATInitMediation;
import com.kidoz.sdk.api.KidozSDK;
import com.kidoz.sdk.api.interfaces.SDKEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KidozATInitManager extends ATInitMediation {

    private static final String TAG = KidozATInitManager.class.getSimpleName();

    private static KidozATInitManager sInstance;

    private boolean mIsIniting;
    private List<InitListener> mListeners;
    private final Object mLock = new Object();


    private KidozATInitManager() {

    }

    public static synchronized KidozATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new KidozATInitManager();
        }
        return sInstance;
    }


    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        this.initSDK(context, serviceExtras, null);
    }

    public void initSDK(Context context, Map<String, Object> serviceExtras, final InitListener initListener) {

        if (!(context instanceof Activity)) {
            return;
        }

        synchronized (mLock) {
            final String publisherId = (String) serviceExtras.get("publisher_id");
            final String securityToken = (String) serviceExtras.get("security_token");

            if (mListeners == null) {
                mListeners = new ArrayList<>();
            }

            if (!KidozSDK.isInitialised()) {
                if (initListener != null) {
                    mListeners.add(initListener);
                }

                if (mIsIniting) {
                    return;
                }

                mIsIniting = true;


                KidozSDK.setSDKListener(new SDKEventListener() {
                    @Override
                    public void onInitSuccess() {
                        callbackResult(true, null);
                    }

                    @Override
                    public void onInitError(String errorMsg) {
                        callbackResult(false, errorMsg);
                    }
                });

                KidozSDK.initialize(((Activity) context), publisherId, securityToken);

            } else {
                if (initListener != null) {
                    initListener.onSuccess();
                }
            }
        }
    }


    private void callbackResult(boolean success, String errorMsg) {
        synchronized (mLock) {
            mIsIniting = false;

            int size = mListeners.size();
            InitListener initListener;
            for (int i = 0; i < size; i++) {
                initListener = mListeners.get(i);
                if (initListener != null) {
                    if (success) {
                        initListener.onSuccess();
                    } else {
                        initListener.onError(errorMsg);
                    }
                }
            }
            mListeners.clear();
        }
    }


    public interface InitListener {
        void onSuccess();

        void onError(String errorMsg);
    }

    @Override
    public String getNetworkName() {
        return "Kidoz";
    }

    @Override
    public String getNetworkVersion() {
        return KidozATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.kidoz.sdk.api.KidozSDK";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.kidoz.sdk.api.ui_views.interstitial.KidozAdActivity");
        return list;
    }
}
