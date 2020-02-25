package com.anythink.network.vungle;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.vungle.warren.AdConfig;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.PlayAdCallback;
import com.vungle.warren.Vungle;

import java.util.Map;

public class VungleATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private final String TAG = VungleATRewardedVideoAdapter.class.getSimpleName();
    String mPlacementId;
    VungleRewardedVideoSetting mSetting;
    AdConfig mAdConfig;

    private final LoadAdCallback loadAdCallback = new LoadAdCallback() {
        @Override
        public void onAdLoad(String s) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdLoaded(VungleATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onError(String s, Throwable throwable) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(VungleATRewardedVideoAdapter.this
                        , ErrorCode.getErrorCode(ErrorCode.noADError, "", throwable.toString()));
            }
        }
    };

    private final PlayAdCallback vungleDefaultListener = new PlayAdCallback() {

        @Override
        public void onAdEnd(@NonNull String placementReferenceId, boolean wasSuccessFulView, boolean wasCallToActionClicked) {
            // Called when user exits the ad and control is returned to your application
            // if wasSuccessfulView is true, the user watched the ad and could be rewarded
            // if wasCallToActionClicked is true, the user clicked the call to action button in the ad.

            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayEnd(VungleATRewardedVideoAdapter.this);
                if (wasSuccessFulView) {
                    mImpressionListener.onReward(VungleATRewardedVideoAdapter.this);
                }
                mImpressionListener.onRewardedVideoAdClosed(VungleATRewardedVideoAdapter.this);

            }

        }

        @Override
        public void onAdStart(@NonNull String placementReferenceId) {
            // Called before playing an ad
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayStart(VungleATRewardedVideoAdapter.this);
            }
        }

        @Override
        public void onError(@NonNull String placementReferenceId, Throwable reason) {
            // Called after playAd(placementId, adConfig) is unable to play the ad
            if (mImpressionListener != null) {
                mImpressionListener.onRewardedVideoAdPlayFailed(VungleATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", reason.toString()));
            }
        }
    };


    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        String mAppId = (String) serverExtras.get("app_id");
        mPlacementId = (String) serverExtras.get("placement_id");

        mLoadResultListener = customRewardVideoListener;

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mPlacementId)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", " appid & placementId is empty."));
            }
            return;
        }

        if (mediationSetting instanceof VungleRewardedVideoSetting) {
            mSetting = (VungleRewardedVideoSetting) mediationSetting;
        }

        mAdConfig = new AdConfig();

        if (mSetting != null) {
            mAdConfig.setAutoRotate(mSetting.getOrientation() == 1);
            mAdConfig.setMuted(mSetting.isSoundEnable());
            mAdConfig.setBackButtonImmediatelyEnabled(mSetting.isBackButtonImmediatelyEnable());
        }

        VungleATInitManager.getInstance().initSDK(activity.getApplicationContext(), serverExtras, new VungleATInitManager.InitListener() {
            @Override
            public void onSuccess() {
                Vungle.loadAd(mPlacementId, loadAdCallback);
            }

            @Override
            public void onError(Throwable throwable) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(VungleATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", throwable.toString()));
                }
            }
        });
    }

    @Override
    public boolean isAdReady() {
        return Vungle.canPlayAd(mPlacementId);
    }

    @Override
    public void show(Activity activity) {
        if (Vungle.canPlayAd(mPlacementId)) {
            // Play a Placement ad with Placement ID, you can pass AdConfig to customize your ad
            Vungle.setIncentivizedFields(mUserId, "", "", "", "");
            Vungle.playAd(mPlacementId, mAdConfig, vungleDefaultListener);
        }
    }

    @Override
    public String getNetworkName() {
        return VungleATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void clean() {
    }

    @Override
    public void onResume(Activity activity) {
    }

    @Override
    public void onPause(Activity activity) {
    }

    @Override
    public String getSDKVersion() {
        return "";
    }

}
