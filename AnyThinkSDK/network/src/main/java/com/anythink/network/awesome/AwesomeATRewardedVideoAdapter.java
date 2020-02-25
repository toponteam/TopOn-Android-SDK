package com.anythink.network.awesome;

import android.app.Activity;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;

import java.util.Map;

import tv.superawesome.sdk.publisher.SAVideoAd;

public class AwesomeATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    int mPlacementId;
    boolean isReward;

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {

        mLoadResultListener = customRewardVideoListener;

        if (serverExtras.containsKey("placement_id")) {
            try {
                mPlacementId = Integer.parseInt(serverExtras.get("placement_id").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mPlacementId == 0) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "placement_id could not be null.");
                mLoadResultListener.onRewardedVideoAdFailed(this, adError);
            }
            return;
        }

        AwesomeATInitManager.getInstance().initSDK(activity, serverExtras);

//        // to display test ads
//        SAVideoAd.enableTestMode ();
//
//        // set configuration to production
//        SAVideoAd.setConfigurationProduction ();
//
//        // lock orientation to portrait or landscape
//        SAVideoAd.setOrientationLandscape ();
//
//        // enable or disable the android back button
//        SAVideoAd.enableBackButton ();
//
//        // enable or disable a close button
//        SAVideoAd.enableCloseButton ();
//
//        // enable or disable auto-closing at the end
////        SAVideoAd.disableCloseAtEnd ();
//        SAVideoAd.enableCloseAtEnd ();
//
//        // make the whole video surface area clickable
//        SAVideoAd.enableSmallClickButton ();

        AwesomeATInitManager.getInstance().putLoadResultAdapter(String.valueOf(mPlacementId), this);
        // start loading ad data for a placement
        SAVideoAd.load(mPlacementId, activity);
    }


    @Override
    public void show(Activity activity) {
        isReward = false;
        // display the ad
        AwesomeATInitManager.getInstance().putAdapter(String.valueOf(mPlacementId), this);
        SAVideoAd.play(mPlacementId, activity);
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public boolean isAdReady() {
        return SAVideoAd.hasAdAvailable(mPlacementId);
    }

    @Override
    public String getSDKVersion() {
        return AwesomeATConst.getSDKVersion();
    }

    @Override
    public void clean() {

    }


    //--------------------------------------------------------------------------
    public void onRewardedVideoAdDataLoaded() {

    }

    public void onRewardedVideoAdLoaded() {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdLoaded(this);
        }
    }

    public void onRewardedVideoAdFailed(String code, String msg) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, code, msg));
        }
    }

    public void onRewardedVideoAdPlayStart() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayStart(this);
        }
    }

    public void onRewardedVideoAdPlayEnd() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd(this);
        }
    }

    public void onRewardedVideoAdPlayFailed(String code, String msg) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayFailed(this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, code, msg));
        }
    }

    public void onRewardedVideoAdClosed() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdClosed(this);
        }
    }

    public void onRewardedVideoAdPlayClicked() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked(this);
        }
    }

    public void onReward() {
        if (mImpressionListener != null) {
            mImpressionListener.onReward(this);
        }
    }

    @Override
    public String getNetworkName() {
        return AwesomeATInitManager.getInstance().getNetworkName();
    }
}
