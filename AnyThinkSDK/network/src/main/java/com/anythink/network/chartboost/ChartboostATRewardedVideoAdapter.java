package com.anythink.network.chartboost;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
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
    private void initAndLoad(Activity activity, Map<String, Object> serverExtras) {
        Chartboost.setActivityCallbacks(false);
        ChartboostATInitManager.getInstance().initSDK(activity, serverExtras, new ChartboostATInitManager.InitCallback() {
            @Override
            public void didInitialize() {
                ChartboostATRewardedVideoAdapter.this.didInitialize();
            }
        });
        Chartboost.onCreate(activity);
        Chartboost.onStart(activity);
//        Chartboost.setShouldPrefetchVideoContent(true);
    }

    @Override
    public void loadRewardVideoAd(final Activity activity, final Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;
        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof ChartboostRewardedVideoSetting) {
            mChartboostMediationSetting = (ChartboostRewardedVideoSetting) mediationSetting;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid ,unitid or sdkkey is empty."));
            }
            return;
        } else {

            String appid = (String) serverExtras.get("app_id");
            String appkey = (String) serverExtras.get("app_signature");
            location = (String) serverExtras.get("location");

            if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(location)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " app_id ,app_signature or location is empty."));
                }
                return;
            }
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initAndLoad(activity, serverExtras);
            }
        });

    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
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
    public void clean() {
        if (mActivityRef.get() != null) {
            Chartboost.onStop(mActivityRef.get());
            Chartboost.onDestroy(mActivityRef.get());
        }

    }

    @Override
    public void onResume(Activity activity) {
        if (mActivityRef.get() != null) {
            Chartboost.onResume(mActivityRef.get());
        }
    }

    @Override
    public void onPause(Activity activity) {
        if (mActivityRef.get() != null) {
            Chartboost.onPause(mActivityRef.get());
        }
    }

    @Override
    public boolean isAdReady() {
        return Chartboost.hasRewardedVideo(location);
    }

    @Override
    public void show(Activity activity) {
        ChartboostATInitManager.getInstance().putAdapter(location, this);
        Chartboost.showRewardedVideo(location);
    }


    @Override
    public String getSDKVersion() {
        return ChartboostATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return ChartboostATInitManager.getInstance().getNetworkName();
    }

    /**
     * -------------------------------------------callback-------------------------------------------------------
     **/

    public boolean shouldDisplayRewardedVideo(String location) {
        return true;
    }

    public void didCacheRewardedVideo(String location) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdLoaded(ChartboostATRewardedVideoAdapter.this);
        }

    }

    public void didFailToLoadRewardedVideo(String location, CBError.CBImpressionError error) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdFailed(ChartboostATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + error.name(), " " + error.toString()));
        }
    }

    public void didDismissRewardedVideo(String location) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdClosed(ChartboostATRewardedVideoAdapter.this);
        }
    }

    public void didCloseRewardedVideo(String location) {
    }

    public void didClickRewardedVideo(String location) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked(ChartboostATRewardedVideoAdapter.this);
        }
    }

    public void didCompleteRewardedVideo(String location, int reward) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd(ChartboostATRewardedVideoAdapter.this);
        }

        if (mImpressionListener != null) {
            mImpressionListener.onReward(ChartboostATRewardedVideoAdapter.this);
        }
    }

    public void didDisplayRewardedVideo(String location) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayStart(ChartboostATRewardedVideoAdapter.this);
        }

    }

    public void willDisplayVideo(String location) {
    }


    public void didInitialize() {
        startload();
    }
}