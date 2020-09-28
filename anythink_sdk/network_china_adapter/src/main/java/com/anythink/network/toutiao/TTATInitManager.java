package com.anythink.network.toutiao;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TTATInitManager extends ATInitMediation {
    public static final String TAG = TTATInitManager.class.getSimpleName();

    private static TTATInitManager sInstance;
    private String mAppId;
    private Handler mHandler;
    private boolean mIsOpenDirectDownload;

    private Map<String, Object> adObject = new ConcurrentHashMap<>();

    private TTATInitManager() {
        mHandler = new Handler(Looper.getMainLooper());
        mIsOpenDirectDownload = true;
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
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public synchronized void initSDK(final Context context, Map<String, Object> serviceExtras, final InitCallback callback) {
        initSDK(context, serviceExtras, false, callback);
    }

    public void initSDK(final Context context, Map<String, Object> serviceExtras, final boolean isSplash, final InitCallback callback) {
        final String appId = (String) serviceExtras.get("app_id");

        if (TextUtils.isEmpty(mAppId) || !TextUtils.equals(mAppId, appId)) {

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
                                .allowShowPageWhenScreenLock(true) //Whether to support display of landing pages in lock screen scenes
                                .directDownloadNetworkType(download) //Allow all network downloads
                                .supportMultiProcess(false) //Whether to support multiple processes, true support
                                .build());

                        doApplicationLifecycleStart(context);

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAppId = appId;
                                if (callback != null) {
                                    callback.onFinish();
                                }
                            }
                        }, isSplash ? 0 : 1000);
                    } catch (Throwable e) {
                        if (callback != null) {
                            callback.onFinish();
                        }
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.onFinish();
            }
        }
    }

    private void doApplicationLifecycleStart(Context context) {
        try {
            //todo
            if (!TTATConst.getNetworkVersion().startsWith("3.2")) {
                return;
            }

            Application application = (Application) context.getApplicationContext();
            Method collectActivityLifecycleCallbacks = Application.class.getDeclaredMethod("collectActivityLifecycleCallbacks");
            collectActivityLifecycleCallbacks.setAccessible(true);
            Object[] objects = (Object[]) collectActivityLifecycleCallbacks.invoke(application);

            for (Object object : objects) {
                try {
                    String name = object.getClass().getName();
                    if (name.startsWith("com.bytedance.sdk.openadsdk")) {
                        ((Application.ActivityLifecycleCallbacks) object).onActivityStarted(null);
                        return;
                    }
                } catch (Throwable e) {
                }
            }
        } catch (Throwable e) {
        }
    }

    public void setIsOpenDirectDownload(boolean download) {
        this.mIsOpenDirectDownload = download;
    }

    interface InitCallback {
        void onFinish();
    }

    @Override
    public String getNetworkName() {
        return "Pangle(Tiktok)";
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
