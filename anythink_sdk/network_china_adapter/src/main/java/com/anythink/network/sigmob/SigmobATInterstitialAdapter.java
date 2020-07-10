package com.anythink.network.sigmob;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.fullscreenvideo.WindFullScreenAdRequest;
import com.sigmob.windad.fullscreenvideo.WindFullScreenVideoAd;
import com.sigmob.windad.fullscreenvideo.WindFullScreenVideoAdListener;
import com.sigmob.windad.rewardedVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAd;

import java.util.Map;

public class SigmobATInterstitialAdapter extends CustomInterstitialAdapter implements WindFullScreenVideoAdListener {

    private static final String TAG = SigmobATInterstitialAdapter.class.getSimpleName();
    private WindFullScreenAdRequest windFullScreenAdRequest;
    private String mPlacementId = "";

    private WindRewardAdRequest windVideoAdRequest;

    boolean isUseRewardedVideoAsInterstital = false;

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;

        String appId = "";
        String appKey = "";
        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service params is empty."));
            }
            return;
        } else {
            if (serverExtras.containsKey("app_id")) {
                appId = serverExtras.get("app_id").toString();
            }
            if (serverExtras.containsKey("app_key")) {
                appKey = serverExtras.get("app_key").toString();
            }
            if (serverExtras.containsKey("placement_id")) {
                mPlacementId = serverExtras.get("placement_id").toString();
            }

            if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey) || TextUtils.isEmpty(mPlacementId)) {
                if (mLoadResultListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id、app_key、placement_id could not be null.");
                    mLoadResultListener.onInterstitialAdLoadFail(this, adError);
                }
                return;
            }
        }
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(SigmobATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context = null."));
            }
            return;
        } else if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(SigmobATInterstitialAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "context must be activity."));
            }
            return;
        }

        if (mediationSetting instanceof SigmobATLocalSetting) {
            isUseRewardedVideoAsInterstital = ((SigmobATLocalSetting) mediationSetting).isUseRewardedVideoAsInterstitial();
        }


        SigmobATInitManager.getInstance().initSDK(context, serverExtras, new SigmobATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                if (isUseRewardedVideoAsInterstital) {
                    windVideoAdRequest = new WindRewardAdRequest(mPlacementId, "", null);
                    SigmobATInitManager.getInstance().loadRewardedVideo(mPlacementId, windVideoAdRequest, SigmobATInterstitialAdapter.this);
                } else {
                    windFullScreenAdRequest = new WindFullScreenAdRequest(mPlacementId, "", null);
                    SigmobATInitManager.getInstance().loadInterstitial(mPlacementId, windFullScreenAdRequest, SigmobATInterstitialAdapter.this);
                }

            }
        });
    }

    @Override
    public void show(Context context) {
        try {
            //Check if the ad is ready
            if (this.isAdReady() && context instanceof Activity) {
                SigmobATInitManager.getInstance().putAdapter(mPlacementId, this);
                //show ad
                if (isUseRewardedVideoAsInterstital) {
                    WindRewardedVideoAd.sharedInstance().show((Activity) context, windVideoAdRequest);
                } else {
                    WindFullScreenVideoAd.sharedInstance().show((Activity) context, windFullScreenAdRequest);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public boolean isAdReady() {
        if (isUseRewardedVideoAsInterstital) {
            return WindRewardedVideoAd.sharedInstance() != null && WindRewardedVideoAd.sharedInstance().isReady(mPlacementId);
        } else {
            return WindFullScreenVideoAd.sharedInstance() != null && WindFullScreenVideoAd.sharedInstance().isReady(mPlacementId);
        }
    }

    @Override
    public String getSDKVersion() {
        return SigmobATConst.getSDKVersion();
    }

    @Override
    public void clean() {
    }

    @Override
    public String getNetworkName() {
        return SigmobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void onFullScreenVideoAdLoadSuccess(String s) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoaded(SigmobATInterstitialAdapter.this);
        }

        try {
            SigmobATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mPlacementId);
        } catch (Throwable e) {

        }
    }

    @Override
    public void onFullScreenVideoAdPreLoadSuccess(String s) {
    }

    @Override
    public void onFullScreenVideoAdPreLoadFail(String s) {
    }

    @Override
    public void onFullScreenVideoAdPlayStart(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow(SigmobATInterstitialAdapter.this);
            mImpressListener.onInterstitialAdVideoStart(SigmobATInterstitialAdapter.this);
        }
    }

    @Override
    public void onFullScreenVideoAdPlayEnd(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoEnd(SigmobATInterstitialAdapter.this);
        }
    }

    @Override
    public void onFullScreenVideoAdClicked(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked(SigmobATInterstitialAdapter.this);
        }
    }

    @Override
    public void onFullScreenVideoAdClosed(String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose(SigmobATInterstitialAdapter.this);
        }

        SigmobATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
    }

    @Override
    public void onFullScreenVideoAdLoadError(WindAdError windAdError, String s) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoadFail(SigmobATInterstitialAdapter.this,
                    ErrorCode.getErrorCode(ErrorCode.noADError, "" + windAdError.getErrorCode(), windAdError.toString()));
        }
    }

    @Override
    public void onFullScreenVideoAdPlayError(WindAdError windAdError, String s) {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdVideoError(SigmobATInterstitialAdapter.this,
                    ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "" + windAdError.getErrorCode(), windAdError.toString()));
        }
    }
}
