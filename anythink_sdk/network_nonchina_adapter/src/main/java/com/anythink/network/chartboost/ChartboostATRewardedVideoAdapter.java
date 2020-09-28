package com.anythink.network.chartboost;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.Model.CBError;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */


public class ChartboostATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    ChartboostRewardedVideoSetting mChartboostMediationSetting;
    String location = CBLocation.LOCATION_DEFAULT;

    /***
     * init and load
     */
    private void initAndLoad(Context context, Map<String, Object> serverExtras) {
        ChartboostATInitManager.getInstance().initSDK(context, serverExtras, new ChartboostATInitManager.InitCallback() {
            @Override
            public void didInitialize() {
                ChartboostATRewardedVideoAdapter.this.didInitialize();
            }
        });
//        Chartboost.setShouldPrefetchVideoContent(true);
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtras, final Map<String, Object> localExtras) {

        String appid = (String) serverExtras.get("app_id");
        String appkey = (String) serverExtras.get("app_signature");
        location = (String) serverExtras.get("location");

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(location)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", " app_id ,app_signature or location is empty.");
            }
            return;
        }

        initAndLoad(context, serverExtras);
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, final Map<String, Object> localExtras) {
        if (serverExtras != null) {
            if (serverExtras.containsKey("app_id") && serverExtras.containsKey("app_signature") && serverExtras.containsKey("location")) {
                location = (String) serverExtras.get("location");
                ChartboostATInitManager.getInstance().putAdapter(location, this);
                return true;
            }
        }
        return false;
    }

    /***
     * load ad
     */
    public void startload() {
        ChartboostATInitManager.getInstance().loadRewardedVideo(location, this);
    }

    @Override
    public void destory() {

    }


    @Override
    public boolean isAdReady() {
        return Chartboost.hasRewardedVideo(location);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return ChartboostATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        ChartboostATInitManager.getInstance().putAdapter(location, this);
        Chartboost.showRewardedVideo(location);
    }


    @Override
    public String getNetworkSDKVersion() {
        return ChartboostATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return ChartboostATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return location;
    }

    /**
     * -------------------------------------------callback-------------------------------------------------------
     **/

    public boolean shouldDisplayRewardedVideo(String location) {
        return true;
    }

    public void didCacheRewardedVideo(String location) {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }

    }

    public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(error.name(), " " + error.toString());
        }
    }

    public void didDismissRewardedVideo(String location) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdClosed();
        }
    }

    public void didCloseRewardedVideo(String location) {
    }

    public void didClickRewardedVideo(String location) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked();
        }
    }

    public void didCompleteRewardedVideo(String location, int reward) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd();
        }

        if (mImpressionListener != null) {
            mImpressionListener.onReward();
        }
    }

    public void didDisplayRewardedVideo(String location) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayStart();
        }

    }

    public void willDisplayVideo(String location) {
    }


    public void didInitialize() {
        try {
            startload();
        } catch (Throwable e) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", e.getMessage());
            }
        }
    }
}