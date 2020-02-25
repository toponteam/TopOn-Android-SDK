package com.anythink.network.chartboost;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Libraries.CBLogging;
import com.chartboost.sdk.Model.CBError;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.GoogleSignatureVerifier;
import com.google.android.gms.tasks.TaskExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChartboostATInitManager extends ATInitMediation {

    private static final String TAG = ChartboostATInitManager.class.getSimpleName();
    private String mAppId;
    private String mAppKey;
    private static ChartboostATInitManager sInstance;

    private ConcurrentHashMap<String, AnyThinkBaseAdapter> mAdapterMap;

    private ConcurrentHashMap<String, AnyThinkBaseAdapter> mLoadResultAdapterMap;

    private InitCallback mInitCallback;

    private ChartboostDelegate delegate = new ChartboostDelegate() {


        @Override
        public boolean shouldRequestInterstitial(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATInterstitialAdapter) {
                return ((ChartboostATInterstitialAdapter) baseAdapter).shouldRequestInterstitial(location);
            }
            return super.shouldRequestInterstitial(location);
        }

        @Override
        public boolean shouldDisplayInterstitial(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATInterstitialAdapter) {
                return ((ChartboostATInterstitialAdapter) baseAdapter).shouldDisplayInterstitial(location);
            }
            return super.shouldDisplayInterstitial(location);
        }

        @Override
        public void didCacheInterstitial(String location) {
            AnyThinkBaseAdapter baseAdapter = mLoadResultAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATInterstitialAdapter) {
                ((ChartboostATInterstitialAdapter) baseAdapter).didCacheInterstitial(location);
            }
            removeLoadResultAdapter(location);
        }

        @Override
        public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
            AnyThinkBaseAdapter baseAdapter = mLoadResultAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATInterstitialAdapter) {
                ((ChartboostATInterstitialAdapter) baseAdapter).didFailToLoadInterstitial(location, error);
            }
            removeLoadResultAdapter(location);
        }

        @Override
        public void willDisplayInterstitial(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATInterstitialAdapter) {
                ((ChartboostATInterstitialAdapter) baseAdapter).willDisplayInterstitial(location);
            }
        }

        @Override
        public void didDismissInterstitial(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATInterstitialAdapter) {
                ((ChartboostATInterstitialAdapter) baseAdapter).didDismissInterstitial(location);
            }
        }

        @Override
        public void didCloseInterstitial(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATInterstitialAdapter) {
                ((ChartboostATInterstitialAdapter) baseAdapter).didCloseInterstitial(location);
            }
            removeAdapter(location);
        }

        @Override
        public void didClickInterstitial(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATInterstitialAdapter) {
                ((ChartboostATInterstitialAdapter) baseAdapter).didClickInterstitial(location);
            }
        }

        @Override
        public void didDisplayInterstitial(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATInterstitialAdapter) {
                ((ChartboostATInterstitialAdapter) baseAdapter).didDisplayInterstitial(location);
            }
        }

        //-------------------------------------------------------------------------

        @Override
        public void didFailToRecordClick(String uri, CBError.CBClickError error) {
            // didFailToRecordClick
        }

        @Override
        public boolean shouldDisplayRewardedVideo(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATRewardedVideoAdapter) {
                return ((ChartboostATRewardedVideoAdapter) baseAdapter).shouldDisplayRewardedVideo(location);
            }
            return true;
        }

        @Override
        public void didCacheRewardedVideo(String location) {
            AnyThinkBaseAdapter baseAdapter = mLoadResultAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATRewardedVideoAdapter) {
                ((ChartboostATRewardedVideoAdapter) baseAdapter).didCacheRewardedVideo(location);
            }
            removeLoadResultAdapter(location);
        }

        @Override
        public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
            AnyThinkBaseAdapter baseAdapter = mLoadResultAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATRewardedVideoAdapter) {
                ((ChartboostATRewardedVideoAdapter) baseAdapter).didFailToLoadRewardedVideo(location, error);
            }
            removeLoadResultAdapter(location);
        }

        @Override
        public void didDismissRewardedVideo(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATRewardedVideoAdapter) {
                ((ChartboostATRewardedVideoAdapter) baseAdapter).didDismissRewardedVideo(location);
            }
        }

        @Override
        public void didCloseRewardedVideo(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATRewardedVideoAdapter) {
                ((ChartboostATRewardedVideoAdapter) baseAdapter).didCloseRewardedVideo(location);
            }
            removeAdapter(location);
        }


        @Override
        public void didClickRewardedVideo(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATRewardedVideoAdapter) {
                ((ChartboostATRewardedVideoAdapter) baseAdapter).didClickRewardedVideo(location);
            }
        }

        @Override
        public void didCompleteRewardedVideo(String location, int reward) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATRewardedVideoAdapter) {
                ((ChartboostATRewardedVideoAdapter) baseAdapter).didCompleteRewardedVideo(location, reward);
            }
        }

        @Override
        public void didDisplayRewardedVideo(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATRewardedVideoAdapter) {
                ((ChartboostATRewardedVideoAdapter) baseAdapter).didDisplayRewardedVideo(location);
            }
        }

        @Override
        public void willDisplayVideo(String location) {
            AnyThinkBaseAdapter baseAdapter = mAdapterMap.get(location);
            if (baseAdapter instanceof ChartboostATRewardedVideoAdapter) {
                ((ChartboostATRewardedVideoAdapter) baseAdapter).willDisplayVideo(location);
            }
        }

        @Override
        public void didCacheInPlay(String location) {
            // Called after a Native Ads object has been loaded from the Chartboost API servers and cached locally.
        }

        @Override
        public void didFailToLoadInPlay(String location, CBError.CBImpressionError error) {
            // Called after a Native Ad has attempted to load from the Chartboost API servers but failed.
        }

        @Override
        public void didInitialize() {
            if (mInitCallback != null) {
                mInitCallback.didInitialize();
            }
        }
    };

    private ChartboostATInitManager() {
        mAdapterMap = new ConcurrentHashMap<>();
        mLoadResultAdapterMap = new ConcurrentHashMap<>();
    }

    public static ChartboostATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new ChartboostATInitManager();
        }
        return sInstance;
    }

    public void initSDK(Activity activity, Map<String, Object> serviceExtras, InitCallback callback) {
        mInitCallback = callback;
        String appId = ((String) serviceExtras.get("app_id"));
        String appKey = ((String) serviceExtras.get("app_signature"));
        Chartboost.setDelegate(delegate);

        if (TextUtils.isEmpty(mAppId) || !mAppId.equals(appId) || !mAppKey.equals(appKey)) {
            if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
                //Whether to agree to collect data
                boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
                //Whether to set the GDPR of the network
                boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");

                if (need_set_gdpr) {
                    Chartboost.setPIDataUseConsent(activity, gdp_consent ? Chartboost.CBPIDataUseConsent.YES_BEHAVIORAL : Chartboost.CBPIDataUseConsent.NO_BEHAVIORAL);
                }

            }

            logGDPRSetting(ChartboostATConst.NETWORK_FIRM_ID);

            Chartboost.startWithAppId(activity, appId, appKey);
            Chartboost.setLoggingLevel(ATSDK.NETWORK_LOG_DEBUG ? CBLogging.Level.ALL : CBLogging.Level.NONE);
            mAppId = appId;
            mAppKey = appKey;
        } else {
            if (callback != null) {
                callback.didInitialize();
            }
        }
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        if (!(context instanceof Activity)) {
            return;
        }

        initSDK((Activity) context, serviceExtras, null);

    }

    public interface InitCallback {
        void didInitialize();
    }

    protected synchronized void putAdapter(String instanceId, AnyThinkBaseAdapter baseAdapter) {
        mAdapterMap.put(instanceId, baseAdapter);
    }

    private synchronized void removeAdapter(String instanceId) {
        mAdapterMap.remove(instanceId);
    }

    protected synchronized void putLoadResultAdapter(String instanceId, AnyThinkBaseAdapter baseAdapter) {
        mLoadResultAdapterMap.put(instanceId, baseAdapter);
    }

    private synchronized void removeLoadResultAdapter(String instanceId) {
        mLoadResultAdapterMap.remove(instanceId);
    }

    public void loadInterstitial(String location, ChartboostATInterstitialAdapter arpuInterstitialAdapter) {
        putLoadResultAdapter(location, arpuInterstitialAdapter);
        Chartboost.cacheInterstitial(location);
    }

    public void loadRewardedVideo(String location, ChartboostATRewardedVideoAdapter arpuInterstitialAdapter) {
        putLoadResultAdapter(location, arpuInterstitialAdapter);
        Chartboost.cacheRewardedVideo(location);
    }

    @Override
    public String getNetworkName() {
        return "Chartboost";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.chartboost.sdk.Chartboost";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();
        pluginMap.put("play-services-ads-identifier-*.aar", false);
        pluginMap.put("play-services-basement-*.aar", false);

        pluginMap.put("play-services-base-*.aar", false);
        pluginMap.put("play-services-tasks-*.aar", false);

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

        try {
            clazz = GooglePlayServicesUtil.class;
            pluginMap.put("play-services-base-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = TaskExecutors.class;
            pluginMap.put("play-services-tasks-*.aar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.chartboost.sdk.CBImpressionActivity");
        return list;
    }
}
