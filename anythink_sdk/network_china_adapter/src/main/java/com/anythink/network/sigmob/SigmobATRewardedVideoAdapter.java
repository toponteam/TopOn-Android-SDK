package com.anythink.network.sigmob;

import android.app.Activity;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.Const;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.rewardedVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardInfo;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAd;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAdListener;

import java.util.Map;

public class SigmobATRewardedVideoAdapter extends CustomRewardVideoAdapter implements WindRewardedVideoAdListener{

    private static final String TAG = SigmobATRewardedVideoAdapter.class.getSimpleName();
    private WindRewardAdRequest windVideoAdRequest;
    private String mPlacementId = "";

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;

        String appId = "";
        String appKey = "";
        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service params is empty."));
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
                    mLoadResultListener.onRewardedVideoAdFailed(this, adError);
                }
                return;
            }
        }

        SigmobATInitManager.getInstance().initSDK(activity, serverExtras, new SigmobATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                windVideoAdRequest = new WindRewardAdRequest(mPlacementId, mUserId, null);
                SigmobATInitManager.getInstance().loadRewardedVideo(mPlacementId, windVideoAdRequest, SigmobATRewardedVideoAdapter.this);
            }
        });
    }

    @Override
    public void show(Activity activity) {
        try {
            //Check if the ad is ready
            if(this.isAdReady()){
                SigmobATInitManager.getInstance().putAdapter(mPlacementId, this);
                //show ad
                WindRewardedVideoAd.sharedInstance().show(activity, windVideoAdRequest);
            }
        } catch (Exception e) {
            if(Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {

    }

    @Override
    public boolean isAdReady() {
        return WindRewardedVideoAd.sharedInstance() != null && WindRewardedVideoAd.sharedInstance().isReady(mPlacementId);
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
    public void onVideoAdLoadSuccess(String placementId) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdLoaded(SigmobATRewardedVideoAdapter.this);
        }

        try {
            SigmobATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mPlacementId);
        } catch (Throwable e) {

        }

    }

    @Override
    public void onVideoAdPreLoadSuccess(String s) {
    }

    @Override
    public void onVideoAdPreLoadFail(String s) {
    }

    @Override
    public void onVideoAdPlayStart(String placementId) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayStart(SigmobATRewardedVideoAdapter.this);
        }
    }

    @Override
    public void onVideoAdPlayEnd(String s) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd(SigmobATRewardedVideoAdapter.this);
        }
    }

    @Override
    public void onVideoAdClicked(String placementId) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked(SigmobATRewardedVideoAdapter.this);
        }

    }

    //The isComplete method in WindRewardInfo returns whether it is completely played
    @Override
    public void onVideoAdClosed(WindRewardInfo windRewardInfo, String placementId) {
        if (mImpressionListener != null) {
            if (windRewardInfo.isComplete()) {
                mImpressionListener.onReward(SigmobATRewardedVideoAdapter.this);
            }
            mImpressionListener.onRewardedVideoAdClosed(SigmobATRewardedVideoAdapter.this);
        }

        SigmobATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
    }

    /**
     * Load ad error callback
     */
    @Override
    public void onVideoAdLoadError(WindAdError windAdError, String placementId) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdFailed(SigmobATRewardedVideoAdapter.this,
                    ErrorCode.getErrorCode(ErrorCode.noADError, "" + windAdError.getErrorCode(), windAdError.toString()));
        }
    }


    /**
     * Playback error
     */
    @Override
    public void onVideoAdPlayError(WindAdError windAdError, String placementId) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayFailed(SigmobATRewardedVideoAdapter.this,
                    ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "" + windAdError.getErrorCode(), windAdError.toString()));
        }
    }

}
