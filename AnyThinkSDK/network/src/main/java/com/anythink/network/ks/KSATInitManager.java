package com.anythink.network.ks;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.kwad.sdk.KsAdSDK;
import com.kwad.sdk.SdkConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KSATInitManager extends ATInitMediation {

    private static final String TAG = KSATInitManager.class.getSimpleName();
    private String mAppId;
    private String mAppName;
    private static KSATInitManager sInstance;

    private KSATInitManager() {

    }

    public static KSATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new KSATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        String app_id = (String) serviceExtras.get("app_id");
        String app_name = (String) serviceExtras.get("app_name");

        if(!TextUtils.isEmpty(app_id) && !TextUtils.isEmpty(app_name)) {
            if(TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mAppName) || !TextUtils.equals(mAppId, app_id) || !TextUtils.equals(mAppName, app_name)) {
                KsAdSDK.init(context, new SdkConfig.Builder()
                        .appId(app_id)
                        .appName(app_name)
                        .debug(ATSDK.NETWORK_LOG_DEBUG)
                        .build());

                mAppId = app_id;
                mAppName = app_name;
            }
        }
    }

    @Override
    public String getNetworkName() {
        return "Kuaishou";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.kwad.sdk.KsAdSDK";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.kwad.sdk.core.page.AdWebViewActivity");
        list.add("com.kwad.sdk.fullscreen.KsFullScreenVideoActivity");
        list.add("com.kwad.sdk.reward.KSRewardVideoActivity");
        list.add("com.kwad.sdk.feed.FeedDownloadActivity");
        return list;
    }

    @Override
    public List getServiceStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.kwai.filedownloader.services.FileDownloadService$SharedMainProcessService");
        list.add("com.kwai.filedownloader.services.FileDownloadService$SeparateProcessService");
        list.add("com.ksad.download.service.DownloadService");
        return list;
    }

    @Override
    public List getProviderStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.kwad.sdk.core.download.provider.AdSdkFileProvider");
        return list;
    }
}
