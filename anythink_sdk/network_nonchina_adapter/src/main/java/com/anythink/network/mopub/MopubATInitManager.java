package com.anythink.network.mopub;

import android.content.Context;

import com.anythink.core.api.ATInitMediation;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.HtmlBannerWebView;
import com.mopub.mobileads.HtmlInterstitialWebView;
import com.mopub.mobileads.MoPubRewardedAd;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.VideoNativeAd;
import com.mopub.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MopubATInitManager extends ATInitMediation {

    private static final String TAG = MopubATInitManager.class.getSimpleName();
    private static MopubATInitManager sInstance;

    private MopubATInitManager() {

    }

    public synchronized static MopubATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new MopubATInitManager();
        }
        return sInstance;
    }


    public synchronized void initSDK(final Context context, final Map<String, Object> serviceExtras, final InitListener initListener) {

        if (MoPub.isSdkInitialized()) {
            initListener.initSuccess();
            return;
        }

        String unitid = (String) serviceExtras.get("unitid");

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(unitid).build();
        MoPub.initializeSdk(context, sdkConfiguration, new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {

                if (initListener != null) {
                    initListener.initSuccess();
                }
            }
        });
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
    public String getNetworkSDKClass() {
        return "com.mopub.common.MoPub";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("mopub-sdk-banner-*.aar", false);
        pluginMap.put("mopub-sdk-interstitial-*.aar", false);
        pluginMap.put("mopub-sdk-native-static-*.aar", false);
        pluginMap.put("mopub-sdk-native-video-*.aar", false);
        pluginMap.put("mopub-sdk-rewardedvideo-*.aar", false);
        pluginMap.put("mopub-volley-*.aar", false);

        Class clazz;
        try {
            clazz = HtmlBannerWebView.class;
            pluginMap.put("mopub-sdk-banner-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = HtmlInterstitialWebView.class;
            pluginMap.put("mopub-sdk-interstitial-*.aar", true);
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
            clazz = VideoNativeAd.class;
            pluginMap.put("mopub-sdk-native-video-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = MoPubRewardedAd.class;
            pluginMap.put("mopub-sdk-rewardedvideo-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = Volley.class;
            pluginMap.put("mopub-volley-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.mopub.common.privacy.ConsentDialogActivity");
        list.add("com.mopub.common.MoPubBrowser");
        list.add("com.mopub.mobileads.MoPubActivity");
        list.add("com.mopub.mobileads.MraidActivity");
        list.add("com.mopub.mobileads.RewardedMraidActivity");
        list.add("com.mopub.mobileads.MraidVideoPlayerActivity");
        return list;
    }
}
