package com.anythink.network.maio;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GoogleSignatureVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.maio.sdk.android.FailNotificationReason;
import jp.maio.sdk.android.MaioAds;
import jp.maio.sdk.android.MaioAdsListener;

public class MaioATInitManager extends ATInitMediation {

    private static final String TAG = MaioATInitManager.class.getSimpleName();
    private String mMediaId;
    private static MaioATInitManager sIntance;
    private ConcurrentHashMap<String, MaioATNotify> mAdapterMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, MaioATNotify> mLoadResultAdapterMap = new ConcurrentHashMap<>();

    MaioAdsListener mListener = new MaioAdsListener() {
        @Override
        public void onFinishedAd(int playtime, boolean skipped, int duration, String zoneEid) {
            MaioATNotify notify = mAdapterMap.get(zoneEid);
            if (notify != null) {
                notify.notifyPlayEnd(!skipped);
            }
        }

        @Override
        public void onClosedAd(String zoneEid) {
            MaioATNotify notify = mAdapterMap.get(zoneEid);
            if (notify != null) {
                notify.notifyClose();
            }
            mAdapterMap.remove(zoneEid);
        }

        public void onInitialized() {
        }

        public void onOpenAd(String zoneEid) {

            MaioATNotify notify = mAdapterMap.get(zoneEid);
            if (notify != null) {
                notify.notifyPlayStart();
            }
        }

        public void onChangedCanShow(String zoneEid, boolean var2) {
            if (var2) {
                MaioATNotify notify = mLoadResultAdapterMap.get(zoneEid);
                if (notify != null) {
                    notify.notifyLoaded();
                }
                mLoadResultAdapterMap.remove(zoneEid);
            }
        }

        public void onStartedAd(String zoneEid) {
        }

        public void onClickedAd(String zoneEid) {
            MaioATNotify notify = mAdapterMap.get(zoneEid);
            if (notify != null) {
                notify.notifyClick();
            }
        }

        public void onFailed(FailNotificationReason var1, String zoneEid) {
            if (var1 == FailNotificationReason.VIDEO) {
                MaioATNotify notify = mAdapterMap.get(zoneEid);
                if (notify != null) {
                    notify.notifyPlayFail("", var1.name());
                }
            } else {
                MaioATNotify notify = mLoadResultAdapterMap.get(zoneEid);
                if (notify != null) {
                    notify.notifyLoadFail("", var1.name());
                }
                mLoadResultAdapterMap.remove(zoneEid);
            }
        }
    };


    public static MaioATInitManager getInstance() {
        if (sIntance == null) {
            sIntance = new MaioATInitManager();
        }
        return sIntance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        if (!(context instanceof Activity)) {
            return;
        }

        MaioAds.setMaioAdsListener(mListener);
        initSDK(((Activity) context), serviceExtras);
    }

    public void initSDK(Activity activity, Map<String, Object> serverExtras) {
        String mediaId = serverExtras.get("media_id").toString();
        if (!TextUtils.isEmpty(mediaId)) {
            if (TextUtils.isEmpty(mMediaId) || !TextUtils.equals(mMediaId, mediaId)) {
                //init
                MaioAds.init(activity, mediaId, mListener);
                mMediaId = mediaId;
            }
        }
    }

    public void addListener(String zoneId, final MaioATNotify adapter) {
        if (mAdapterMap != null) {
            mAdapterMap.put(zoneId, adapter);
        }
    }

    public void addLoadResultListener(String zoneId, final MaioATNotify adapter) {
        if (mLoadResultAdapterMap != null) {
            mLoadResultAdapterMap.put(zoneId, adapter);
        }
    }

    @Override
    public String getNetworkName() {
        return "Maio";
    }

    @Override
    public String getNetworkSDKClass() {
        return "jp.maio.sdk.android.MaioAds";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("play-services-ads-identifier-*.aar", false);
        pluginMap.put("play-services-basement-*.aar", false);

        Class clazz;
        try {
            clazz = AdvertisingIdClient.class;
            pluginMap.put("play-services-ads-identifier-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = GoogleSignatureVerifier.class;
            pluginMap.put("play-services-basement-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("jp.maio.sdk.android.AdFullscreenActivity");
        list.add("jp.maio.sdk.android.HtmlBasedAdActivity");
        return list;
    }
}
