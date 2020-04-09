package com.anythink.network.toutiao;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.androidquery.AQuery;
import com.anythink.core.api.ATInitMediation;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;

public class TTATInitManager extends ATInitMediation {
    public static final String TAG = TTATInitManager.class.getSimpleName();

    private static TTATInitManager sInstance;
    private String mAppId;
    private Handler mHandler;

    private TTATInitManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static TTATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new TTATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public void initSDK(final Context context, Map<String, Object> serviceExtras, final InitCallback callback) {
        final String appId = (String) serviceExtras.get("app_id");

        if (TextUtils.isEmpty(mAppId) || !TextUtils.equals(mAppId, appId)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    TTAdSdk.init(context.getApplicationContext(), new TTAdConfig.Builder()
                            .appId(appId)
                            .useTextureView(true) //Use the TextureView control to play the video. The default is SurfaceView. When there are conflicts in SurfaceView, you can use TextureView
                            .appName(context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString())
                            .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                            .allowShowPageWhenScreenLock(true) //Whether to support display of landing pages in lock screen scenes
                            .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_MOBILE, TTAdConstant.NETWORK_STATE_2G, TTAdConstant.NETWORK_STATE_3G,
                                    TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_4G) //Allow all network downloads
                            .supportMultiProcess(false) //Whether to support multiple processes, true support
                            .build());

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAppId = appId;
                            if (callback != null) {
                                callback.onFinish();
                            }
                        }
                    }, 1000);
                }
            });
        } else {
            if (callback != null) {
                callback.onFinish();
            }
        }
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

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();

        pluginMap.put("android-gif-drawable-*.aar", false);
        pluginMap.put("ndroid-query-full.*.aar", false);

        Class clazz;
        try {
            clazz = GifDrawable.class;
            pluginMap.put("android-gif-drawable-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = AQuery.class;
            pluginMap.put("ndroid-query-full.*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.ss.android.socialbase.appdownloader.view.DownloadTaskDeleteActivity");
        list.add("com.ss.android.downloadlib.activity.TTDelegateActivity");
        list.add("com.bytedance.sdk.openadsdk.activity.TTLandingPageActivity");
        list.add("com.bytedance.sdk.openadsdk.activity.TTPlayableLandingPageActivity");
        list.add("com.bytedance.sdk.openadsdk.activity.TTVideoLandingPageActivity");
        list.add("com.bytedance.sdk.openadsdk.activity.TTRewardVideoActivity");
        list.add("com.bytedance.sdk.openadsdk.activity.TTRewardExpressVideoActivity");
        list.add("com.bytedance.sdk.openadsdk.activity.TTFullScreenVideoActivity");
        list.add("com.bytedance.sdk.openadsdk.activity.TTFullScreenExpressVideoActivity");
        list.add("com.bytedance.sdk.openadsdk.activity.TTDelegateActivity");
        return list;
    }

    @Override
    public List getServiceStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.ss.android.socialbase.downloader.notification.DownloadNotificationService");
        list.add("com.ss.android.socialbase.downloader.downloader.DownloadService");
        list.add("com.ss.android.socialbase.downloader.downloader.IndependentProcessDownloadService");
        list.add("com.ss.android.socialbase.downloader.impls.DownloadHandleService");
        list.add("com.ss.android.socialbase.appdownloader.DownloadHandlerService");
        list.add("com.bytedance.sdk.openadsdk.multipro.aidl.BinderPoolService");
        return list;
    }

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.bytedance.sdk.openadsdk.multipro.TTMultiProvider");
        list.add("com.bytedance.sdk.openadsdk.TTFileProvider");
        return list;
    }
}
