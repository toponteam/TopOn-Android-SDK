package com.anythink.network.flurry;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.flurry.android.FlurryAgent;
import com.flurry.android.ads.FlurryAdErrorType;
import com.flurry.android.ads.FlurryAdInterstitial;
import com.flurry.android.ads.FlurryAdInterstitialListener;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */

public class FlurryATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private static final String TAG = FlurryATRewardedVideoAdapter.class.getSimpleName();

    FlurryAdInterstitial mFlurryAdInterstitial;

    FlurryRewardedVideoSetting mFlurryMediationSetting;

    String placeid = "";

    /***
     * load ad
     */
    private void startLoad(Activity activity) {

        final Context applicationContext = activity.getApplicationContext();
        mIsShow = false;

        mFlurryAdInterstitial = new FlurryAdInterstitial(activity, placeid);
        mFlurryAdInterstitial.setListener(new FlurryAdInterstitialListener() {
            @Override
            public void onFetched(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (applicationContext != null) {
                    FlurryAgent.onEndSession(applicationContext);
                }
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(FlurryATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onRendered(FlurryAdInterstitial pFlurryAdInterstitial) {

            }

            @Override
            public void onDisplay(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(FlurryATRewardedVideoAdapter.this);
                }

            }

            @Override
            public void onClose(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressionListener != null && mIsShow) {
                    mIsShow = false;
                    mImpressionListener.onRewardedVideoAdClosed(FlurryATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onAppExit(FlurryAdInterstitial pFlurryAdInterstitial) {
            }

            @Override
            public void onClicked(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(FlurryATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onVideoCompleted(FlurryAdInterstitial pFlurryAdInterstitial) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(FlurryATRewardedVideoAdapter.this);
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward(FlurryATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onError(FlurryAdInterstitial pFlurryAdInterstitial, FlurryAdErrorType pFlurryAdErrorType, int pI) {
                if (applicationContext != null) {
                    FlurryAgent.onEndSession(applicationContext);
                }
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(FlurryATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, pI + "", pFlurryAdErrorType.toString()));
                }

            }
        });

        if (activity != null) {
            FlurryAgent.onStartSession(activity.getApplicationContext());
        }
        if (check()) {
            mFlurryAdInterstitial.fetchAd();
        }
    }

    @Override
    public void clean() {
        if (check()) {
            mFlurryAdInterstitial.destroy();
        }
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }


    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;
        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof FlurryRewardedVideoSetting) {
            mFlurryMediationSetting = (FlurryRewardedVideoSetting) mediationSetting;

        }

        String sdkKey = "";
        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid or unitid  is empty."));
            }
            return;
        } else {

            sdkKey = ((String) serverExtras.get("sdk_key"));
            placeid = ((String) serverExtras.get("ad_space"));

            if (TextUtils.isEmpty(sdkKey) || TextUtils.isEmpty(placeid)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "flurry sdkkey is empty."));
                }
                return;
            }
        }
        FlurryATInitManager.getInstance().initSDK(activity, serverExtras);
        startLoad(activity);
    }

    @Override
    public boolean isAdReady() {
        if (check()) {
            return mFlurryAdInterstitial.isReady();
        }
        return false;
    }

    /***
     * Whether to show
     */
    boolean mIsShow;

    @Override
    public void show(Activity activity) {
        if (check() && isAdReady()) {
            mIsShow = true;
            mFlurryAdInterstitial.displayAd();
        }
    }


    private boolean check() {
        return mFlurryAdInterstitial != null;
    }

    @Override
    public String getSDKVersion() {
        return FlurryATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return FlurryATInitManager.getInstance().getNetworkName();
    }
}