package com.anythink.network.unityads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.metadata.PlayerMetaData;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */
public class UnityAdsATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    private static final String TAG = UnityAdsATRewardedVideoAdapter.class.getSimpleName();

    UnityAdsRewardedVideoSetting mUnityAdRewardVideoSetting;
    String placement_id = "";

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;
        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof UnityAdsRewardedVideoSetting) {
            mUnityAdRewardVideoSetting = (UnityAdsRewardedVideoSetting) mediationSetting;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {
            String game_id = (String) serverExtras.get("game_id");
            placement_id = (String) serverExtras.get("placement_id");

            if (TextUtils.isEmpty(game_id) || TextUtils.isEmpty(placement_id)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "unityads game_id, placement_id is empty!"));
                }
                return;
            }
        }

        PlayerMetaData playerMetaData = new PlayerMetaData(activity.getApplicationContext());
        playerMetaData.setServerId(mUserId);
        playerMetaData.commit();

        UnityAds.PlacementState placementState = UnityAds.getPlacementState(placement_id);
        if (UnityAds.PlacementState.READY == placementState) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdLoaded(this);
            }
        } else {
            UnityAdsATInitManager.getInstance().putLoadResultAdapter(placement_id, this);
            UnityAdsATInitManager.getInstance().initSDK(activity, serverExtras);
            UnityAds.load(placement_id);
        }
    }

    @Override
    public boolean isAdReady() {
        return UnityAds.isReady(placement_id);
    }

    @Override
    public void show(Activity activity) {
        UnityAdsATInitManager.getInstance().putAdapter(placement_id, this);
        UnityAds.show(activity, placement_id);
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
        if (serverExtras != null) {
            if (serverExtras.containsKey("game_id") && serverExtras.containsKey("placement_id")) {
                placement_id = (String) serverExtras.get("placement_id");
                return true;
            }
        }
        return false;
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


    void notifyLoaded(String placementId) {
        if (mLoadResultListener != null && placement_id.equals(placementId)) {
            mLoadResultListener.onRewardedVideoAdLoaded(UnityAdsATRewardedVideoAdapter.this);
        }
    }

    void notifyLoadFail(String code, String msg) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdFailed(UnityAdsATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, code, msg));
        }
    }

    void notifyStart(String placementId) {
        if (mImpressionListener != null && placement_id.equals(placementId)) {
            mImpressionListener.onRewardedVideoAdPlayStart(UnityAdsATRewardedVideoAdapter.this);
        }
    }

    void notifyFinish(String placementId, UnityAds.FinishState finishState) {
        if (mImpressionListener != null && placement_id.equals(placementId)) {
            switch (finishState) {
                case ERROR:
                    mImpressionListener.onRewardedVideoAdPlayFailed(UnityAdsATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.rewardedVideoPlayError, "", " play video error"));
                    mImpressionListener.onRewardedVideoAdClosed(UnityAdsATRewardedVideoAdapter.this);
                    break;
                case COMPLETED:
                    mImpressionListener.onRewardedVideoAdPlayEnd(UnityAdsATRewardedVideoAdapter.this);
                    mImpressionListener.onReward(UnityAdsATRewardedVideoAdapter.this);
                    mImpressionListener.onRewardedVideoAdClosed(UnityAdsATRewardedVideoAdapter.this);
                    break;
                case SKIPPED:
                    mImpressionListener.onRewardedVideoAdPlayEnd(UnityAdsATRewardedVideoAdapter.this);
                    mImpressionListener.onRewardedVideoAdClosed(UnityAdsATRewardedVideoAdapter.this);
                    break;
                default:
                    break;

            }
        }
    }

    void notifyClick(String placementId) {
        if (mImpressionListener != null && placement_id.equals(placementId)) {
            mImpressionListener.onRewardedVideoAdPlayClicked(UnityAdsATRewardedVideoAdapter.this);
        }
    }

    @Override
    public String getSDKVersion() {
        return UnityAdsATConst.getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return UnityAdsATInitManager.getInstance().getNetworkName();
    }

}