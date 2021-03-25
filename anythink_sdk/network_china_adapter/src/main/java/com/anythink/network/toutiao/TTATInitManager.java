/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.toutiao;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.anythink.core.api.ATInitMediation;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


public class TTATInitManager extends ATInitMediation {
    public static final String TAG = TTATInitManager.class.getSimpleName();

    private static TTATInitManager sInstance;
    private Handler mHandler;
    private boolean mIsOpenDirectDownload;

    private Map<String, Object> adObject = new ConcurrentHashMap<>();

    private boolean mHasInit;
    private AtomicBoolean mIsIniting;
    private List<InitCallback> mListeners;
    private final Object mLock = new Object();

    private TTATInitManager() {
        mHandler = new Handler(Looper.getMainLooper());
        mIsOpenDirectDownload = true;
        mIsIniting = new AtomicBoolean(false);
    }

    public synchronized static TTATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new TTATInitManager();
        }
        return sInstance;
    }

    protected void put(String adsourceId, Object object) {
        adObject.put(adsourceId, object);
    }

    protected void remove(String adsourceId) {
        adObject.remove(adsourceId);
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public void initSDK(final Context context, Map<String, Object> serviceExtras, final InitCallback callback) {

        if (TTAdSdk.isInitSuccess() || mHasInit) {
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }

        synchronized (mLock) {

            if (mIsIniting.get()) {
                if (callback != null) {
                    mListeners.add(callback);
                }
                return;
            }

            if (mListeners == null) {
                mListeners = new ArrayList<>();
            }

            mIsIniting.set(true);
        }

        final String appId = (String) serviceExtras.get("app_id");

        if (callback != null) {
            mListeners.add(callback);
        }

        final int[] download;
        if (mIsOpenDirectDownload) {
            download = new int[]{
                    TTAdConstant.NETWORK_STATE_MOBILE, TTAdConstant.NETWORK_STATE_2G, TTAdConstant.NETWORK_STATE_3G,
                    TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_4G
            };
        } else {
            download = new int[]{
                    TTAdConstant.NETWORK_STATE_2G
            };
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    TTAdSdk.init(context.getApplicationContext(), new TTAdConfig.Builder()
                            .appId(appId)
                            .useTextureView(true) //Use the TextureView control to play the video. The default is SurfaceView. When there are conflicts in SurfaceView, you can use TextureView
                            .appName(context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString())
                            .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                            .directDownloadNetworkType(download) //Allow all network downloads
                            .supportMultiProcess(false) //Whether to support multiple processes, true support
                            .build(), new TTAdSdk.InitCallback() {
                        @Override
                        public void success() {
                            mHasInit = true;

                            callbackResult(true, null, null);
                        }

                        @Override
                        public void fail(int errorCode, String errorMsg) {
                            callbackResult(false, errorCode + "", errorMsg);
                        }
                    });

                } catch (Throwable e) {
                    callbackResult(false, "", e.getMessage());
                }
            }
        });
    }

    private void callbackResult(boolean success, String errorCode, String errorMsg) {
        synchronized (mLock) {
            int size = mListeners.size();
            InitCallback initListener;
            for (int i = 0; i < size; i++) {
                initListener = mListeners.get(i);
                if (initListener != null) {
                    if (success) {
                        initListener.onSuccess();
                    } else {
                        initListener.onError(errorCode, errorMsg);
                    }
                }
            }
            mListeners.clear();

            mIsIniting.set(false);
        }
    }


    public void setIsOpenDirectDownload(boolean download) {
        this.mIsOpenDirectDownload = download;
    }

    interface InitCallback {
        void onSuccess();

        void onError(String errorCode, String errorMsg);

    }

    @Override
    public String getNetworkName() {
        return "Pangle(Tiktok)";
    }

    @Override
    public String getNetworkVersion() {
        return TTATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.bytedance.sdk.openadsdk.TTAdSdk";
    }

//    @Override
//    public Map<String, Boolean> getPluginClassStatus() {
//        HashMap<String, Boolean> pluginMap = new HashMap<>();
//
//        pluginMap.put("android-gif-drawable-*.aar", false);
//        pluginMap.put("android-query-full.*.aar", false);
//
//        Class clazz;
//        try {
//            clazz = GifDrawable.class;
//            pluginMap.put("android-gif-drawable-*.aar", true);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//        try {
//            clazz = AQuery.class;
//            pluginMap.put("android-query-full.*.aar", true);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//        return pluginMap;
//    }

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.bytedance.sdk.openadsdk.multipro.TTMultiProvider");
        list.add("com.bytedance.sdk.openadsdk.TTFileProvider");
        return list;
    }
}
