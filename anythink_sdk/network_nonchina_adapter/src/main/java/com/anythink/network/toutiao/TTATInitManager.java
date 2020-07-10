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

                    TTAdConfig.Builder builder = new TTAdConfig.Builder()
                            .appId(appId)
                            .useTextureView(true) //Use the TextureView control to play the video. The default is SurfaceView. When there are conflicts in SurfaceView, you can use TextureView
                            .appName(context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString())
                            .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
                            .allowShowPageWhenScreenLock(true) //Whether to support display of landing pages in lock screen scenes
                            .supportMultiProcess(false); //Whether to support multiple processes, true support

                    //GDPR
                    if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
                        //Whether to agree to collect data
                        boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
                        //Whether to set the GDPR of the network
                        boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");

                        if (need_set_gdpr) {
                            builder.setGDPR(gdp_consent ? 0 : 1);//Fields to indicate whether you are protected by GDPR,  the value of GDPR : 0 close GDRP Privacy protection ，1: open GDRP Privacy protection
                        }
                    }


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
