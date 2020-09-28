package com.anythink.network.sigmob;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.base.Const;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.rewardedVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardInfo;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAd;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAdListener;

import java.util.Map;

public class SigmobATRewardedVideoAdapter extends CustomRewardVideoAdapter implements WindRewardedVideoAdListener {

    private static final String TAG = SigmobATRewardedVideoAdapter.class.getSimpleName();
    private WindRewardAdRequest windVideoAdRequest;
    private String mPlacementId = "";

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String appId = "";
        String appKey = "";

        if (serverExtra.containsKey("app_id")) {
            appId = serverExtra.get("app_id").toString();
        }
        if (serverExtra.containsKey("app_key")) {
            appKey = serverExtra.get("app_key").toString();
        }
        if (serverExtra.containsKey("placement_id")) {
            mPlacementId = serverExtra.get("placement_id").toString();
        }

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey) || TextUtils.isEmpty(mPlacementId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "app_id、app_key、placement_id could not be null.");
            }
            return;
        }

        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    SigmobATInitManager.getInstance().initSDK(context.getApplicationContext(), serverExtra, new SigmobATInitManager.InitCallback() {
                        @Override
                        public void onFinish() {
                            windVideoAdRequest = new WindRewardAdRequest(mPlacementId, mUserId, null);
                            SigmobATInitManager.getInstance().loadRewardedVideo(mPlacementId, windVideoAdRequest, SigmobATRewardedVideoAdapter.this);
                        }
                    });
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }

            }
        });

    }

    @Override
    public void show(Activity activity) {
        try {
            //Check if the ad is ready
            if (activity != null) {
                if (this.isAdReady()) {
                    SigmobATInitManager.getInstance().putAdapter(mPlacementId, this);
                    //show ad
                    WindRewardedVideoAd.sharedInstance().show(activity, windVideoAdRequest);
                }
            }
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isAdReady() {
        return WindRewardedVideoAd.sharedInstance() != null && WindRewardedVideoAd.sharedInstance().isReady(mPlacementId);
    }

    @Override
    public String getNetworkName() {
        return SigmobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        windVideoAdRequest = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mPlacementId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return SigmobATConst.getSDKVersion();
    }


    @Override
    public void onVideoAdLoadSuccess(String placementId) {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
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
            mImpressionListener.onRewardedVideoAdPlayStart();
        }
    }

    @Override
    public void onVideoAdPlayEnd(String s) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd();
        }
    }

    @Override
    public void onVideoAdClicked(String placementId) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked();
        }

    }

    //The isComplete method in WindRewardInfo returns whether it is completely played
    @Override
    public void onVideoAdClosed(WindRewardInfo windRewardInfo, String placementId) {
        if (mImpressionListener != null) {
            if (windRewardInfo.isComplete()) {
                mImpressionListener.onReward();
            }
            mImpressionListener.onRewardedVideoAdClosed();
        }

        SigmobATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
    }

    /**
     * Load ad error callback
     */
    @Override
    public void onVideoAdLoadError(WindAdError windAdError, String placementId) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError("" + windAdError.getErrorCode(), windAdError.toString());
        }
    }

    /**
     * Playback error
     */
    @Override
    public void onVideoAdPlayError(WindAdError windAdError, String placementId) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayFailed("" + windAdError.getErrorCode(), windAdError.toString());
        }
    }

}
