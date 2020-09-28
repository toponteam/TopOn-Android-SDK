package com.anythink.network.mintegral;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.advanced.view.MTGNativeAdvancedView;
import com.mintegral.msdk.interstitial.view.MTGInterstitialActivity;
import com.mintegral.msdk.interstitialvideo.out.MTGInterstitialVideoHandler;
import com.mintegral.msdk.mtgbanner.view.MTGBannerWebView;
import com.mintegral.msdk.mtgbid.out.BidManager;
import com.mintegral.msdk.mtgjscommon.base.BaseWebView;
import com.mintegral.msdk.mtgnative.a.b;
import com.mintegral.msdk.nativex.view.MTGMediaView;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.playercommon.PlayerView;
import com.mintegral.msdk.reward.player.MTGRewardVideoActivity;
import com.mintegral.msdk.splash.view.MTGSplashView;
import com.mintegral.msdk.video.js.bridge.BaseVideoBridge;
import com.mintegral.msdk.video.js.bridge.RewardJs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MintegralATInitManager extends ATInitMediation {

    public static final String TAG = MintegralATInitManager.class.getSimpleName();

    private String mAppId;
    private String mAppKey;
    private final Handler mHandler;
    private static MintegralATInitManager sInstance;
    private Map<String, Object> adObject = new ConcurrentHashMap<>();

    private MintegralATInitManager() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public synchronized static MintegralATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new MintegralATInitManager();
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

    public synchronized void initSDK(final Context context, final Map<String, Object> serviceExtras, final InitCallback callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String appid = (String) serviceExtras.get("appid");
                String appkey = (String) serviceExtras.get("appkey");

                if (!TextUtils.isEmpty(appid) && !TextUtils.isEmpty(appkey)) {
                    try {
                        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAppKey) || !TextUtils.equals(mAppId, appid) || !TextUtils.equals(mAppKey, appkey)) {
                            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
                            Map<String, String> map = sdk.getMTGConfigurationMap(appid, appkey);
//                            MIntegralConstans.DEBUG = ATSDK.isNetworkLogDebug();

                            sdk.init(map, context.getApplicationContext());
                            mAppId = appid;
                            mAppKey = appkey;

                            mAppId = appid;
                            mAppKey = appkey;

                            if (callback != null) {
                                callback.onSuccess();
                            }
                        } else {
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();

                        if (callback != null) {
                            callback.onError(e);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();

        int open = isConsent ? MIntegralConstans.IS_SWITCH_ON : MIntegralConstans.IS_SWITCH_OFF;
        String level = MIntegralConstans.AUTHORITY_ALL_INFO;
        sdk.setUserPrivateInfoType(context, level, open);

        return true;
    }

    public interface InitCallback {
        void onSuccess();

        void onError(Throwable e);
    }

    @Override
    public String getNetworkName() {
        return "Mintegral";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.mintegral.msdk.MIntegralSDK";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("mintegral_alphab.aar", false);
        pluginMap.put("mintegral_interstitial.aar", false);
        pluginMap.put("mintegral_interstitialvideo.aar", false);
        pluginMap.put("mintegral_mtgbanner.aar", false);
        pluginMap.put("mintegral_mtgbid.aar", false);
        pluginMap.put("mintegral_mtgjscommon.aar", false);
        pluginMap.put("mintegral_mtgnative.aar", false);
        pluginMap.put("mintegral_nativeex.aar", false);
        pluginMap.put("mintegral_playercommon.aar", false);
        pluginMap.put("mintegral_reward.aar", false);
        pluginMap.put("mintegral_videocommon.aar", false);
        pluginMap.put("mintegral_videojs.aar", false);
        pluginMap.put("mintegral_mtgnativeadvanced.aar", false);
        pluginMap.put("mintegral_mtgsplash.aar", false);


        Class clazz;

        try {
            clazz = MTGInterstitialActivity.class;
            pluginMap.put("mintegral_interstitial.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MTGInterstitialVideoHandler.class;
            pluginMap.put("mintegral_interstitialvideo.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MTGBannerWebView.class;
            pluginMap.put("mintegral_mtgbanner.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = BidManager.class;
            pluginMap.put("mintegral_mtgbid.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = BaseWebView.class;
            pluginMap.put("mintegral_mtgjscommon.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = b.class;
            pluginMap.put("mintegral_mtgnative.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MTGMediaView.class;
            pluginMap.put("mintegral_nativeex.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = PlayerView.class;
            pluginMap.put("mintegral_playercommon.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MTGRewardVideoActivity.class;
            pluginMap.put("mintegral_reward.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = RewardJs.class;
            pluginMap.put("mintegral_videocommon.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = BaseVideoBridge.class;
            pluginMap.put("mintegral_videojs.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MTGNativeAdvancedView.class;
            pluginMap.put("mintegral_mtgnativeadvanced.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MTGSplashView.class;
            pluginMap.put("mintegral_mtgsplash.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

//        try {
//            if(MintegralATConst.isChinaSdk()) {
//                //国内版
//                Class.forName("com.mintegral.msdk.pluginFramework.PluginService");
//                pluginMap.put("mintegral_mtgdownloads.aar", true);
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.mintegral.msdk.activity.MTGCommonActivity");
        list.add("com.mintegral.msdk.reward.player.MTGRewardVideoActivity");
        list.add("com.mintegral.msdk.interstitial.view.MTGInterstitialActivity");
        return list;
    }

    @Override
    public List getServiceStatus() {
        return null;
    }

    @Override
    public List getProviderStatus() {
        return null;

    }
}
