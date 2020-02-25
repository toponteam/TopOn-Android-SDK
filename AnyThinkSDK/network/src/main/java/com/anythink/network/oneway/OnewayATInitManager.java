package com.anythink.network.oneway;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.didi.virtualapk.PluginManager;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.breakpoint.RemitSyncExecutor;
import com.liulishuo.okdownload.core.connection.DownloadOkHttp3Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mobi.oneway.common.OwBFileProvider;
import mobi.oneway.export.Ad.OWInterstitialAd;
import mobi.oneway.export.Ad.OWRewardedAd;
import mobi.oneway.export.Ad.OnewaySdk;
import mobi.oneway.export.AdListener.OWInterstitialAdListener;
import mobi.oneway.export.AdListener.OWRewardedAdListener;
import okhttp3.OkHttpClient;
import okio.Okio;

public class OnewayATInitManager extends ATInitMediation {

    private static final String TAG = OnewayATInitManager.class.getSimpleName();
    private boolean isInit;
    private boolean isInitRewardVideo;
    private boolean isInitInterstitial;

    private static OnewayATInitManager sIntance;

    private OnewayATInitManager() {
        isInit = false;
    }

    public static OnewayATInitManager getInstance() {
        if (sIntance == null) {
            sIntance = new OnewayATInitManager();
        }
        return sIntance;
    }

    public boolean isInit() {
        return isInit;
    }

    public boolean isInitRewardVideo() {
        return isInitRewardVideo;
    }

    public boolean isInitInterstitial() {
        return isInitInterstitial;
    }

    public void initRewardVideo(Activity activity, OWRewardedAdListener listener) {
        if (!isInitRewardVideo) {
            isInitRewardVideo = true;
            OWRewardedAd.init(activity, listener);
        }
    }

    public void initInterstitial(Activity activity, OWInterstitialAdListener listener) {
        if (!isInitInterstitial) {
            isInitInterstitial = true;
            OWInterstitialAd.init(activity, listener);
        }
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        if (!isInit) {
            OnewaySdk.init(context);
            OnewaySdk.setDebugMode(ATSDK.NETWORK_LOG_DEBUG);
            isInit = true;
        }
    }

    @Override
    public String getNetworkName() {
        return "Oneway";
    }

    @Override
    public String getNetworkSDKClass() {
        return "mobi.oneway.export.Ad.OnewaySdk";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("liulishuo-okdownload-*.aar", false);
        pluginMap.put("liulishuo-okhttp-*.aar", false);
        pluginMap.put("liulishuo-sqlite-*.aar", false);
        pluginMap.put("okhttp-*.jar", false);
        pluginMap.put("okio-*.jar", false);
        pluginMap.put("oneway-mobi-core-*.aar", false);
        pluginMap.put("virtualapk-core-*.aar", false);

        Class clazz;
        try {
            clazz = DownloadTask.class;
            pluginMap.put("liulishuo-okdownload-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = DownloadOkHttp3Connection.class;
            pluginMap.put("liulishuo-okhttp-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = RemitSyncExecutor.class;
            pluginMap.put("liulishuo-sqlite-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = OkHttpClient.class;
            pluginMap.put("okhttp-*.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = Okio.class;
            pluginMap.put("okio-*.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = OwBFileProvider.class;
            pluginMap.put("oneway-mobi-core-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = PluginManager.class;
            pluginMap.put("virtualapk-core-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("mobi.oneway.export.AdShowActivity");
        list.add("com.didi.virtualapk.delegate.StubActivity");
        return list;
    }


    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("mobi.oneway.export.OWProvider");
        list.add("mobi.oneway.common.OwGFileProvider");
        list.add("mobi.oneway.common.OwBFileProvider");
        list.add("com.liulishuo.okdownload.OkDownloadProvider");
        list.add("com.didi.virtualapk.delegate.RemoteContentProvider");
        return list;
    }
}
