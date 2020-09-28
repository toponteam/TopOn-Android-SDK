package com.anythink.network.gdt;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
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
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String posId = "";
        String appid = "";

        if (serverExtra.containsKey("app_id")) {
            appid = serverExtra.get("app_id").toString();
        }

        if (serverExtra.containsKey("unit_id")) {
            posId = serverExtra.get("unit_id").toString();
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(posId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "GTD appid or unitId is empty.");

            }
            return;
        }

        mUnitId = posId;
        isReady = false;

        GDTATInitManager.getInstance().initSDK(context, serverExtra, new GDTATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context);
            }

            @Override
            public void onError() {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "GTD initSDK failed.");
                }
            }
        });
    }

    private void startLoadAd(Context context) {
        mRewardVideoAD = new RewardVideoAD(context.getApplicationContext(), mUnitId, new RewardVideoADListener() {
            @Override
            public void onADLoad() {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onVideoCached() {
                isReady = true;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }

                try {
                    GDTATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mRewardVideoAD);
                } catch (Exception e) {

                }

            }

            @Override
            public void onADShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onADExpose() {

            }

            @Override
            public void onReward() {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            @Override
            public void onADClick() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }

            @Override
            public void onVideoComplete() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }
            }

            @Override
            public void onADClose() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }

                try {
                    GDTATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
                } catch (Exception e) {

                }

            }

            @Override
            public void onError(com.qq.e.comm.util.AdError adError) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(adError.getErrorCode() + "", adError.getErrorMsg());
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
    public void destory() {
        mRewardVideoAD = null;
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return GDTATConst.getNetworkVersion();
    }

    @Override
    public boolean isAdReady() {
        return mRewardVideoAD != null && !mRewardVideoAD.hasShown();
    }

    @Override
    public void show(Activity activity) {
        if (isReady) {
            try {
                if (activity != null) {
                    mRewardVideoAD.showAD(activity);
                    isReady = false;
                } else {
                    mRewardVideoAD.showAD();
                    isReady = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
