package com.anythink.network.gdt;

import android.app.Activity;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;

import java.util.Map;

/**
 * Copyright (C) 2018 {XX} Science and Technology Co., Ltd.
 */
public class GDTATRewardedVideoAdapter extends CustomRewardVideoAdapter {
    RewardVideoAD mRewardVideoAD;


    String mUnitId;
    boolean isReady = false;

    @Override
    public void loadRewardVideoAd(final Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {

        String posId = "";
        String appid = "";

        if (serverExtras.containsKey("app_id")) {
            appid = serverExtras.get("app_id").toString();
        }

        if (serverExtras.containsKey("unit_id")) {
            posId = serverExtras.get("unit_id").toString();
        }

        mLoadResultListener = customRewardVideoListener;
        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(posId)) {
            if (mLoadResultListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "GTD appid or unitId is empty.");
                mLoadResultListener.onRewardedVideoAdFailed(this, adError);

            }
            return;
        }


        mUnitId = posId;
        isReady = false;

        GDTATInitManager.getInstance().initSDK(activity, serverExtras, new GDTATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(activity, mUnitId);
            }

            @Override
            public void onError() {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(GDTATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "GTD initSDK failed."));
                }
            }
        });
    }

    private void startLoadAd(Activity activity, String posId) {
        mRewardVideoAD = new RewardVideoAD(activity.getApplicationContext(), posId, new RewardVideoADListener() {
            @Override
            public void onADLoad() {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdDataLoaded(GDTATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onVideoCached() {
                isReady = true;
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdLoaded(GDTATRewardedVideoAdapter.this);
                }

                try {
                    GDTATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mRewardVideoAD);
                } catch (Exception e) {

                }

            }

            @Override
            public void onADShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart(GDTATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onADExpose() {

            }

            @Override
            public void onReward() {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward(GDTATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onADClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked(GDTATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onVideoComplete() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd(GDTATRewardedVideoAdapter.this);
                }
            }

            @Override
            public void onADClose() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed(GDTATRewardedVideoAdapter.this);
                }

                try {
                    GDTATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
                } catch (Exception e) {

                }

            }

            @Override
            public void onError(com.qq.e.comm.util.AdError adError) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(GDTATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, adError.getErrorCode() + "", adError.getErrorMsg()));
                }
            }
        });
        mRewardVideoAD.loadAD();
    }

    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {
        return mRewardVideoAD != null && !mRewardVideoAD.hasShown();
    }

    @Override
    public void show(Activity activity) {
        if (isReady) {
            mRewardVideoAD.showAD(activity);
            isReady = false;
        }

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
        return GDTATConst.getNetworkVersion();
    }

}
