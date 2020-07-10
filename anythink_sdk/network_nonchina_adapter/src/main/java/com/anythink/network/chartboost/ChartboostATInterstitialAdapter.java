package com.anythink.network.chartboost;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.chartboost.sdk.CBLocation;
import com.chartboost.sdk.Chartboost;
import com.chartboost.sdk.Model.CBError;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */


public class ChartboostATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = ChartboostATInterstitialAdapter.class.getSimpleName();

    ChartboostRewardedVideoSetting mChartboostMediationSetting;
    String location = CBLocation.LOCATION_DEFAULT;

    boolean isRewared = false;

    /***
     * init and load
     */
    private void initAndLoad(Activity activity, Map<String, Object> serverExtras) {
        ChartboostATInitManager.getInstance().initSDK(activity, serverExtras, new ChartboostATInitManager.InitCallback() {
            @Override
            public void didInitialize() {
                ChartboostATInterstitialAdapter.this.didInitialize();
            }
        });
//        Chartboost.setShouldPrefetchVideoContent(true);
    }

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof ChartboostRewardedVideoSetting) {
            mChartboostMediationSetting = (ChartboostRewardedVideoSetting) mediationSetting;

        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " serverExtras is empty."));
            }
            return;
        } else {
            String appid = (String) serverExtras.get("app_id");
            String appkey = (String) serverExtras.get("app_signature");
            location = (String) serverExtras.get("location");

            if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(location)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " app_id ,app_signature or location is empty."));
                }
                return;
            }
        }

        if (!(context instanceof Activity)) {
            mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " context must be activity."));
            return;
        }
        initAndLoad((Activity) context, serverExtras);

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
        ChartboostATInitManager.getInstance().loadInterstitial(location, this);
    }

    @Override
    public void clean() {
    }

    @Override
    public String getNetworkName() {
        return ChartboostATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public boolean isAdReady() {
        return Chartboost.hasInterstitial(location);
    }

    @Override
    public void show(Context context) {
        ChartboostATInitManager.getInstance().putAdapter(location, this);
        Chartboost.showInterstitial(location);
    }


    @Override
    public String getSDKVersion() {
        return ChartboostATConst.getNetworkVersion();
    }


    /**
     * -------------------------------------------callback-------------------------------------------------------
     **/

    public boolean shouldRequestInterstitial(String location) {
        return true;
    }

    public boolean shouldDisplayInterstitial(String location) {
        return true;
    }

    public void didCacheInterstitial(String location) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoaded(ChartboostATInterstitialAdapter.this);
        }
    }

    public void didFailToLoadInterstitial(String location, CBError.CBImpressionError error) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoadFail(ChartboostATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + error.name(), " " + error.toString()));
        }
    }

    public void willDisplayInterstitial(String location) {
    }

    public void didDismissInterstitial(String location) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose(ChartboostATInterstitialAdapter.this);
        }
    }

    public void didCloseInterstitial(String location) {

    }

    public void didClickInterstitial(String location) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked(ChartboostATInterstitialAdapter.this);
        }
    }

    public void didCompleteInterstitial(String location) {
        isRewared = true;
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoEnd(ChartboostATInterstitialAdapter.this);
        }
    }

    public void didDisplayInterstitial(String location) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow(ChartboostATInterstitialAdapter.this);
            mImpressListener.onInterstitialAdVideoStart(ChartboostATInterstitialAdapter.this);
        }
    }

    public void didInitialize() {
        startload();
    }
}