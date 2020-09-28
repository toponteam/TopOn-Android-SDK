package com.anythink.network.toutiao;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TTATInitManager extends ATInitMediation {
    public static final String TAG = TTATInitManager.class.getSimpleName();

    private static TTATInitManager sInstance;
    private String mAppId;
    private Handler mHandler;

    private TTATInitManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public synchronized static TTATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new TTATInitManager();
        }
        return sInstance;
    }


    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    public synchronized void initSDK(final Context context, final Map<String, Object> serviceExtras, final InitCallback callback) {
        final String appId = (String) serviceExtras.get("app_id");

        if (TextUtils.isEmpty(mAppId) || !TextUtils.equals(mAppId, appId)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (builder == null) {
                        builder = new TTAdConfig.Builder();
                    }

                    builder.appId(appId)
                            .useTextureView(false) //Use the TextureView control to play the video. The default is SurfaceView. When there are conflicts in SurfaceView, you can use TextureView
                            .appName(context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString())
                            .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                            .allowShowPageWhenScreenLock(true) //Whether to support display of landing pages in lock screen scenes
                            .supportMultiProcess(false); //Whether to support multiple processes, true support


                    TTAdSdk.init(context.getApplicationContext(),
                            builder.build());

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

    TTAdConfig.Builder builder = new TTAdConfig.Builder();

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        if (builder == null) {
            builder = new TTAdConfig.Builder();
        }
        builder.setGDPR(isConsent ? 0 : 1);
        return true;
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
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.bytedance.sdk.openadsdk.multipro.TTMultiProvider");
        return list;
    }
}
