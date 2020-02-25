package com.anythink.network.unityads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATInitMediation;
import com.unity3d.ads.metadata.MetaData;
import com.unity3d.services.UnityServices;
import com.unity3d.services.monetization.IUnityMonetizationListener;
import com.unity3d.services.monetization.UnityMonetization;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UnityAdsATInitManager extends ATInitMediation {

    private static final String TAG = UnityAdsATInitManager.class.getSimpleName();
    private String mGameId;
    private static UnityAdsATInitManager sIntance;
    private ConcurrentHashMap<String, Object> mLoadResultAdapterMap = new ConcurrentHashMap<>();

    private IUnityMonetizationListener mListener = new IUnityMonetizationListener() {
        @Override
        public void onPlacementContentReady(String placementId, PlacementContent placementContent) {
            Object adapter = mLoadResultAdapterMap.get(placementId);
            try {
                if (adapter instanceof UnityAdsATInterstitialAdapter) {
                    ((UnityAdsATInterstitialAdapter) adapter).notifyLoaded(placementId);
                }
            } catch (Throwable e) {

            }

            try {
                if (adapter instanceof UnityAdsATRewardedVideoAdapter) {
                    ((UnityAdsATRewardedVideoAdapter) adapter).notifyLoaded(placementId);
                }
            } catch (Throwable e) {

            }
            removeLoadResultAdapter(placementId);
        }

        @Override
        public void onPlacementContentStateChange(String s, PlacementContent placementContent, UnityMonetization.PlacementContentState placementContentState, UnityMonetization.PlacementContentState placementContentState1) {

        }

        @Override
        public void onUnityServicesError(UnityServices.UnityServicesError unityServicesError, String s) {

            for (Object adapter : mLoadResultAdapterMap.values()) {
                try {
                    if (adapter instanceof UnityAdsATInterstitialAdapter) {
                        ((UnityAdsATInterstitialAdapter) adapter).notifyLoadFail(unityServicesError.name(), s);
                    }
                } catch (Throwable e) {

                }
                try {
                    if (adapter instanceof UnityAdsATRewardedVideoAdapter) {
                        ((UnityAdsATRewardedVideoAdapter) adapter).notifyLoadFail(unityServicesError.name(), s);
                    }
                } catch (Throwable e) {

                }
            }
            mLoadResultAdapterMap.clear();
        }
    };

    private UnityAdsATInitManager() {

    }

    public static UnityAdsATInitManager getInstance() {
        if (sIntance == null) {
            sIntance = new UnityAdsATInitManager();
        }
        return sIntance;
    }


    protected synchronized void putLoadResultAdapter(String placementId, final Object adapter) {
        mLoadResultAdapterMap.put(placementId, adapter);
    }

    protected synchronized void removeLoadResultAdapter(String placementId) {
        mLoadResultAdapterMap.remove(placementId);
    }

    protected void supportGDPR(Context context, Map<String, Object> serviceExtras) {
        try {

            if (serviceExtras.containsKey("gdpr_consent") && serviceExtras.containsKey("need_set_gdpr")) {
                //Whether to agree to collect data
                boolean gdp_consent = (boolean) serviceExtras.get("gdpr_consent");
                //Whether to set the GDPR of the network
                boolean need_set_gdpr = (boolean) serviceExtras.get("need_set_gdpr");
                if (need_set_gdpr) {
                    MetaData gdprMetaData = new MetaData(context.getApplicationContext());
                    gdprMetaData.set("gdpr.consent", gdp_consent);
                    gdprMetaData.commit();
                }
            }
            logGDPRSetting(UnityAdsATConst.NETWORK_FIRM_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initSDK(Context context, Map<String, Object> serviceExtras) {
        if (!(context instanceof Activity)) {
            return;
        }

        String game_id = (String) serviceExtras.get("game_id");
        if (!TextUtils.isEmpty(game_id)) {
            if (TextUtils.isEmpty(mGameId) || !TextUtils.equals(mGameId, game_id)) {
                supportGDPR(context.getApplicationContext(), serviceExtras);
                UnityMonetization.initialize(((Activity) context), game_id, mListener);
                mGameId = game_id;
            }
            UnityMonetization.setListener(mListener);
        }
    }

    @Override
    public String getNetworkName() {
        return "UnityAds";
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.unity3d.services.monetization.UnityMonetization";
    }

    @Override
    public List getActivityStatus() {
        ArrayList<String> list = new ArrayList<>();
        list.add("com.unity3d.services.ads.adunit.AdUnitActivity");
        list.add("com.unity3d.services.ads.adunit.AdUnitTransparentActivity");
        list.add("com.unity3d.services.ads.adunit.AdUnitTransparentSoftwareActivity");
        list.add("com.unity3d.services.ads.adunit.AdUnitSoftwareActivity");
        return list;
    }
}
