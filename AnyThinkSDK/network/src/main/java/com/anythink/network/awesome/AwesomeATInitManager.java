package com.anythink.network.awesome;

import android.app.Application;
import android.content.Context;

import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.moat.analytics.mobile.sup.MoatAdEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tv.superawesome.lib.saadloader.SALoader;
import tv.superawesome.lib.sabumperpage.SABumperPage;
import tv.superawesome.lib.saevents.SAEvents;
import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.samoatevents.SAMoatEvents;
import tv.superawesome.lib.samodelspace.saad.SAAd;
import tv.superawesome.lib.sanetwork.request.SANetwork;
import tv.superawesome.lib.saparentalgate.SAParentalGate;
import tv.superawesome.lib.sasession.session.SASession;
import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.lib.savastparser.SAVASTParser;
import tv.superawesome.lib.savideoplayer.SAVideoPlayer;
import tv.superawesome.lib.sawebplayer.SAWebPlayer;
import tv.superawesome.sagdprisminorsdk.minor.process.GetIsMinorProcess;
import tv.superawesome.sdk.publisher.AwesomeAds;
import tv.superawesome.sdk.publisher.SAEvent;
import tv.superawesome.sdk.publisher.SAInterface;
import tv.superawesome.sdk.publisher.SAVideoAd;

public class AwesomeATInitManager extends ATInitMediation {

    private static final String TAG = AwesomeATInitManager.class.getSimpleName();
    private boolean mIsInit;
    private static AwesomeATInitManager sInstance;

    private ConcurrentHashMap<String, AnyThinkBaseAdapter> mAdapterMap;

    private ConcurrentHashMap<String, AnyThinkBaseAdapter> mLoadResultAdapterMap;

    SAInterface saInterface = new SAInterface() {
        @Override
        public void onEvent(int placement_id, SAEvent saEvent) {
            AnyThinkBaseAdapter baseAdapter = null;
            switch (saEvent) {
                case adLoaded:
                case adAlreadyLoaded:
                    baseAdapter = mLoadResultAdapterMap.get(String.valueOf(placement_id));
                    if (baseAdapter instanceof AwesomeATRewardedVideoAdapter) {
                        ((AwesomeATRewardedVideoAdapter) baseAdapter).onRewardedVideoAdLoaded();
                    }
                    removeLoadResultAdapter(String.valueOf(placement_id));

                    break;
                case adShown:
                    baseAdapter = mAdapterMap.get(String.valueOf(placement_id));
                    if (baseAdapter instanceof AwesomeATRewardedVideoAdapter) {
                        ((AwesomeATRewardedVideoAdapter) baseAdapter).onRewardedVideoAdPlayStart();
                    }
                    break;
                case adEmpty:
                case adFailedToLoad:
                    baseAdapter = mLoadResultAdapterMap.get(String.valueOf(placement_id));
                    if (baseAdapter instanceof AwesomeATRewardedVideoAdapter) {
                        ((AwesomeATRewardedVideoAdapter) baseAdapter).onRewardedVideoAdFailed("", saEvent.name());
                    }
                    removeLoadResultAdapter(String.valueOf(placement_id));
                    break;
                case adFailedToShow:
                    baseAdapter = mAdapterMap.get(String.valueOf(placement_id));
                    if (baseAdapter instanceof AwesomeATRewardedVideoAdapter) {
                        ((AwesomeATRewardedVideoAdapter) baseAdapter).onRewardedVideoAdPlayFailed("", saEvent.name());
                    }
                    removeAdapter(String.valueOf(placement_id));
                    break;
                case adEnded:
                    baseAdapter = mAdapterMap.get(String.valueOf(placement_id));
                    if (baseAdapter instanceof AwesomeATRewardedVideoAdapter) {
                        ((AwesomeATRewardedVideoAdapter) baseAdapter).onRewardedVideoAdPlayEnd();
                        ((AwesomeATRewardedVideoAdapter) baseAdapter).onReward();
                    }

                    break;
                case adClosed:
                    baseAdapter = mAdapterMap.get(String.valueOf(placement_id));
                    if (baseAdapter instanceof AwesomeATRewardedVideoAdapter) {
                        ((AwesomeATRewardedVideoAdapter) baseAdapter).onRewardedVideoAdClosed();
                    }
                    removeAdapter(String.valueOf(placement_id));
                    break;
                case adClicked:
                    baseAdapter = mAdapterMap.get(String.valueOf(placement_id));
                    if (baseAdapter instanceof AwesomeATRewardedVideoAdapter) {
                        ((AwesomeATRewardedVideoAdapter) baseAdapter).onRewardedVideoAdPlayClicked();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private AwesomeATInitManager() {
        mAdapterMap = new ConcurrentHashMap<>();
        mLoadResultAdapterMap = new ConcurrentHashMap<>();
    }

    public static AwesomeATInitManager getInstance() {
        if (sInstance == null) {
            sInstance = new AwesomeATInitManager();
        }
        return sInstance;
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        if (!mIsInit) {
            AwesomeAds.init(((Application) context.getApplicationContext()), ATSDK.NETWORK_LOG_DEBUG);
            mIsInit = true;
        }
        SAVideoAd.setListener(saInterface);
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

    @Override
    public String getNetworkName() {
        return "Superawesome";
    }

    @Override
    public String getNetworkSDKClass() {
        return "tv.superawesome.sdk.publisher.AwesomeAds";
    }

    @Override
    public Map<String, Boolean> getPluginClassStatus() {
        HashMap<String, Boolean> pluginMap = new HashMap<>();

        pluginMap.put("moatlib.jar", false);
        pluginMap.put("saadloader.jar", false);
        pluginMap.put("sabumperpage.jar", false);
        pluginMap.put("saevents.jar", false);
        pluginMap.put("sagdprisminorsdk.jar", false);
        pluginMap.put("sajsonparser.jar", false);
        pluginMap.put("samoatevents.jar", false);
        pluginMap.put("samodelspace.jar", false);
        pluginMap.put("sanetwork.jar", false);
        pluginMap.put("saparentalgate.jar", false);
        pluginMap.put("sasession.jar", false);
        pluginMap.put("sautils.jar", false);
        pluginMap.put("savastparser.jar", false);
        pluginMap.put("savideoplayer.jar", false);
        pluginMap.put("sawebplayer.jar", false);

        Class clazz;
        try {
            clazz = MoatAdEvent.class;
            pluginMap.put("moatlib.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SALoader.class;
            pluginMap.put("saadloader.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SABumperPage.class;
            pluginMap.put("sabumperpage.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SAEvents.class;
            pluginMap.put("saevents.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = GetIsMinorProcess.class;
            pluginMap.put("sagdprisminorsdk.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SAJsonParser.class;
            pluginMap.put("sajsonparser.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SAMoatEvents.class;
            pluginMap.put("samoatevents.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SAAd.class;
            pluginMap.put("samodelspace.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SANetwork.class;
            pluginMap.put("sanetwork.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SAParentalGate.class;
            pluginMap.put("saparentalgate.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SASession.class;
            pluginMap.put("sasession.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SAUtils.class;
            pluginMap.put("sautils.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SAVASTParser.class;
            pluginMap.put("savastparser.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clazz = SAVideoPlayer.class;
            pluginMap.put("savideoplayer.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            clazz = SAWebPlayer.class;
            pluginMap.put("sawebplayer.jar", true);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return pluginMap;
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("tv.superawesome.sdk.publisher.SAVideoAd");
        list.add("tv.superawesome.sdk.publisher.SAInterstitialAd");
        list.add("tv.superawesome.lib.sabumperpage.SABumperPage");
        return list;
    }

}
