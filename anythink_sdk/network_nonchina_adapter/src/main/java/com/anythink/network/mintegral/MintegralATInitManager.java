/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.mintegral;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.mbridge.msdk.MBridgeConstans;
import com.mbridge.msdk.MBridgeSDK;
import com.mbridge.msdk.advanced.view.MBNativeAdvancedView;
import com.mbridge.msdk.interactiveads.activity.InteractiveShowActivity;
import com.mbridge.msdk.interstitial.view.MBInterstitialActivity;
import com.mbridge.msdk.interstitialvideo.out.MBInterstitialVideoHandler;
import com.mbridge.msdk.mbbanner.view.MBBannerWebView;
import com.mbridge.msdk.mbbid.out.BidManager;
import com.mbridge.msdk.mbnative.c.b;
import com.mbridge.msdk.mbsignalcommon.base.BaseWebView;
import com.mbridge.msdk.nativex.view.MBMediaView;
import com.mbridge.msdk.out.ChannelManager;
import com.mbridge.msdk.out.MBridgeSDKFactory;
import com.mbridge.msdk.playercommon.PlayerView;
import com.mbridge.msdk.reward.player.MBRewardVideoActivity;
import com.mbridge.msdk.splash.view.MBSplashView;
import com.mbridge.msdk.video.signal.communication.BaseRewardSignalH5;
import com.mbridge.msdk.video.signal.communication.RewardSignal;

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
                            ChannelManager.setChannel("Y+H6DFttYrPQYcIeicKwJQKQYrN=");
                            MBridgeSDK sdk = MBridgeSDKFactory.getMBridgeSDK();
                            Map<String, String> map = sdk.getMBConfigurationMap(appid, appkey);
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
        MBridgeSDK sdk = MBridgeSDKFactory.getMBridgeSDK();

        int open = isConsent ? MBridgeConstans.IS_SWITCH_ON : MBridgeConstans.IS_SWITCH_OFF;
        String level = MBridgeConstans.AUTHORITY_ALL_INFO;
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
    public String getNetworkVersion() {
        return MintegralATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.mbridge.msdk.MBridgeSDK";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("mbridge_interactiveads.aar", false);
        pluginMap.put("mbridge_interstitial.aar", false);
        pluginMap.put("mbridge_interstitialvideo.aar", false);
        pluginMap.put("mbridge_mbbanner.aar", false);
        pluginMap.put("mbridge_mbbid.aar", false);
        pluginMap.put("mbridge_mbjscommon.aar", false);
        pluginMap.put("mbridge_mbnative.aar", false);
        pluginMap.put("mbridge_nativeex.aar", false);
        pluginMap.put("mbridge_playercommon.aar", false);
        pluginMap.put("mbridge_reward.aar", false);
        pluginMap.put("mbridge_videocommon.aar", false);
        pluginMap.put("mbridge_videojs.aar", false);
        pluginMap.put("mbridge_mbnativeadvanced.aar", false);
        pluginMap.put("mbridge_mbsplash.aar", false);

        //exoplayer
        pluginMap.put("exoplayer_common.aar", false);
        pluginMap.put("exoplayer_core.aar", false);
        pluginMap.put("exoplayer_extractor.aar", false);


        Class clazz;

        try {
            clazz = InteractiveShowActivity.class;
            pluginMap.put("mbridge_interactiveads.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MBInterstitialActivity.class;
            pluginMap.put("mbridge_interstitial.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MBInterstitialVideoHandler.class;
            pluginMap.put("mbridge_interstitialvideo.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MBBannerWebView.class;
            pluginMap.put("mbridge_mbbanner.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = BidManager.class;
            pluginMap.put("mbridge_mbbid.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = BaseWebView.class;
            pluginMap.put("mbridge_mbjscommon.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = b.class;
            pluginMap.put("mbridge_mbnative.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MBMediaView.class;
            pluginMap.put("mbridge_nativeex.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = PlayerView.class;
            pluginMap.put("mbridge_playercommon.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MBRewardVideoActivity.class;
            pluginMap.put("mbridge_reward.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = RewardSignal.class;
            pluginMap.put("mbridge_videocommon.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = BaseRewardSignalH5.class;
            pluginMap.put("mbridge_videojs.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MBNativeAdvancedView.class;
            pluginMap.put("mbridge_mbnativeadvanced.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MBSplashView.class;
            pluginMap.put("mbridge_mbsplash.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

//        try {
//            if(MintegralATConst.isCnSdk()) {
//                //国内版
//                Class.forName("com.mintegral.msdk.pluginFramework.PluginService");
//                pluginMap.put("mintegral_mtgdownloads.aar", true);
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }


        try {
            Class.forName("com.google.android.exoplayer2.ExoPlayerLibraryInfo");
            pluginMap.put("exoplayer_common.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            Class.forName("com.google.android.exoplayer2.ExoPlayer");
            pluginMap.put("exoplayer_core.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            Class.forName("com.google.android.exoplayer2.extractor.SeekMap");
            pluginMap.put("exoplayer_extractor.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

}
