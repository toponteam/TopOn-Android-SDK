package com.anythink.network.maio;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;

import java.util.Map;

import jp.maio.sdk.android.MaioAds;

public class MaioATRewardedVideoAdapter extends CustomRewardVideoAdapter implements MaioATNotify {

    String mZoneId;

    @Override
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        String mMediaId = "";
        if (serverExtras.containsKey("media_id")) {
            mMediaId = serverExtras.get("media_id").toString();
        }

        if (serverExtras.containsKey("zone_id")) {
            mZoneId = serverExtras.get("zone_id").toString();
        }

        mLoadResultListener = customRewardVideoListener;
        if (TextUtils.isEmpty(mMediaId) || TextUtils.isEmpty(mZoneId)) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", " mediaid or zoneid  is empty.");
                mLoadResultListener.onRewardedVideoAdFailed(this, adError);
            }
            return;
        }

        if (MaioAds.canShow(mZoneId)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdLoaded(this);
            }
            return;
        }

        MaioATInitManager.getInstance().addLoadResultListener(mZoneId, this);
        MaioATInitManager.getInstance().initSDK(activity, serverExtras);
    }

    @Override
    public boolean initNetworkObjectByPlacementId(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting) {
        if (serverExtras != null) {
            if (serverExtras.containsKey("zone_id")) {
                mZoneId = serverExtras.get("zone_id").toString();
            }
            return true;
        }
        return false;
    }

    @Override
    public void show(Activity activity) {
        if (MaioAds.canShow(mZoneId)) {
            MaioATInitManager.getInstance().addListener(mZoneId, this);
            MaioAds.show(mZoneId);
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
        return MaioAds.canShow(mZoneId);
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return MaioATInitManager.getInstance().getNetworkName();
    }

    //internal callback
    @Override
    public void notifyLoaded() {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdLoaded(this);
        }
    }

    @Override
    public void notifyLoadFail(String code, String msg) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, code, msg));
        }
    }

    @Override
    public void notifyPlayStart() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayStart(this);
        }
    }

    @Override
    public void notifyClick() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked(this);
        }
    }

    @Override
    public void notifyClose() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdClosed(this);
        }
    }

    @Override
    public void notifyPlayEnd(boolean isReward) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd(this);
        }

        if (mImpressionListener != null) {
            mImpressionListener.onReward(this);
        }

    }

    @Override
    public void notifyPlayFail(String code, String msg) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, code, msg));
        }
    }

    @Override
    public String getSDKVersion() {
        return MaioATConst.getNetworkVersion();
    }
}
