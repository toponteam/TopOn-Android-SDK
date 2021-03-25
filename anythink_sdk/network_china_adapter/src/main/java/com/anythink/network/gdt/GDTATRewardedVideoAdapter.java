/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.gdt;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.ads.rewardvideo2.ExpressRewardVideoAD;
import com.qq.e.ads.rewardvideo2.ExpressRewardVideoAdListener;
import com.qq.e.comm.util.AdError;
import com.qq.e.comm.util.VideoAdValidity;

import java.util.Map;

public class GDTATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private static final String TAG = GDTATRewardedVideoAdapter.class.getSimpleName();
    RewardVideoAD mRewardVideoAD;
    ExpressRewardVideoAD mExpressRewardVideoAD;

    String mUnitId;
    boolean isReady = false;
    private String mPersonalizedTemplate = "0";
    private int mVideoMuted = 0;

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

        if (serverExtra.containsKey("personalized_template")) {
            mPersonalizedTemplate = serverExtra.get("personalized_template").toString();
        }

        if (serverExtra.containsKey("video_muted")) {
            mVideoMuted = Integer.parseInt(serverExtra.get("video_muted").toString());
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
        switch (mPersonalizedTemplate) {
            case "1":
                loadExpressRewardVideoAD(context);
                break;

            case "0":
            default:
                loadRewardVideoAD(context);
                break;
        }
    }

    private void loadRewardVideoAD(Context context) {
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
            public void onReward(Map<String, Object> map) {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            //For old version
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
            public void onError(AdError adError) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(adError.getErrorCode() + "", adError.getErrorMsg());
                }
            }
        }, mVideoMuted != 1);

        try {
            ServerSideVerificationOptions.Builder builder = new ServerSideVerificationOptions.Builder();
            builder.setUserId(mUserId);
            builder.setCustomData(mUserData);
            mRewardVideoAD.setServerSideVerificationOptions(builder.build());
        } catch (Throwable e) {
        }

        mRewardVideoAD.loadAD();
    }

    private void loadExpressRewardVideoAD(Context context) {
        mExpressRewardVideoAD = new ExpressRewardVideoAD(context, mUnitId, new ExpressRewardVideoAdListener() {
            @Override
            public void onAdLoaded() {
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
                    GDTATInitManager.getInstance().put(getTrackingInfo().getmUnitGroupUnitId(), mExpressRewardVideoAD);
                } catch (Exception e) {

                }
            }

            @Override
            public void onShow() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onExpose() {

            }

            //For old version
            public void onReward() {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            @Override
            public void onReward(Map<String, Object> map) {
                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            @Override
            public void onClick() {
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
            public void onClose() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }

                try {
                    GDTATInitManager.getInstance().remove(getTrackingInfo().getmUnitGroupUnitId());
                } catch (Exception e) {

                }

            }

            @Override
            public void onError(AdError adError) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError(adError.getErrorCode() + "", adError.getErrorMsg());
                }
            }
        });

        mExpressRewardVideoAD.setVolumeOn(mVideoMuted != 1);
        try {
            ServerSideVerificationOptions.Builder builder = new ServerSideVerificationOptions.Builder();
            builder.setUserId(mUserId);
            builder.setCustomData(mUserData);
            mExpressRewardVideoAD.setServerSideVerificationOptions(builder.build());
        } catch (Throwable e) {
        }

        mExpressRewardVideoAD.loadAD();

    }


    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void destory() {
        if (mRewardVideoAD != null) {
            mRewardVideoAD = null;
        }

        if (mExpressRewardVideoAD != null) {
            mExpressRewardVideoAD.destroy();
            mExpressRewardVideoAD = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return GDTATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public boolean isAdReady() {

        if (mRewardVideoAD != null) {
            return isReady && !mRewardVideoAD.hasShown();
        }

        if (mExpressRewardVideoAD != null) {
            return isReady && !mExpressRewardVideoAD.hasShown();
        }

        return false;
    }

    @Override
    public void show(Activity activity) {

        if (mRewardVideoAD != null) {
            try {
                if (activity != null) {
                    mRewardVideoAD.showAD(activity);
                } else {
                    mRewardVideoAD.showAD();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mExpressRewardVideoAD != null) {

            if (activity != null) {
                mExpressRewardVideoAD.showAD(activity);
            } else {
                Log.e(TAG, "GDT native express reward video, show: activity = null");
            }
        }

        isReady = false;
    }

}
