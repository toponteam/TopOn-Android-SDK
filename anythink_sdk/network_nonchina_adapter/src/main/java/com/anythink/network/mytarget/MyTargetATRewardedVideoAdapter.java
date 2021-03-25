/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.network.mytarget;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.my.target.ads.Reward;
import com.my.target.ads.RewardedAd;
import com.my.target.common.MyTargetManager;

import java.util.Map;

public class MyTargetATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    private static final String TAG = MyTargetATRewardedVideoAdapter.class.getSimpleName();

    private int mSlotId = -1;
    private boolean mIsReady;
    private RewardedAd mRewardedAd;
    private String mPayload;

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            mIsReady = false;
            mRewardedAd.show();
        }
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {

        String slotId = (String) serverExtra.get("slot_id");

        if (TextUtils.isEmpty(slotId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "MyTarget slot_id = null");
            }
            return;
        }

        mSlotId = Integer.parseInt(slotId);

        mPayload = (String) serverExtra.get("payload");

        startLoadAd(context);
    }

    private void startLoadAd(Context context) {
        mRewardedAd = new RewardedAd(mSlotId, context);
        mRewardedAd.setListener(new RewardedAd.RewardedAdListener() {
            @Override
            public void onLoad(@NonNull RewardedAd rewardedAd) {
                mIsReady = true;
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onNoAd(@NonNull String reason, @NonNull RewardedAd rewardedAd) {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "MyTarget " + reason);
                }
            }

            @Override
            public void onClick(@NonNull RewardedAd rewardedAd) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayClicked();
                }
            }

            @Override
            public void onDismiss(@NonNull RewardedAd rewardedAd) {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onReward(@NonNull Reward reward, @NonNull RewardedAd rewardedAd) {

                Log.i(TAG, "onReward: " + reward.type);

                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                }

                if (mImpressionListener != null) {
                    mImpressionListener.onReward();
                }
            }

            @Override
            public void onDisplay(@NonNull RewardedAd rewardedAd) {
                mIsReady = false;
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }
        });

        if (!TextUtils.isEmpty(mPayload)) {
            mRewardedAd.loadFromBid(mPayload);
        } else {
            mRewardedAd.load();
        }
    }

    @Override
    public void destory() {
        if (mRewardedAd != null) {
            mRewardedAd.setListener(null);
            mRewardedAd.destroy();
            mRewardedAd = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return String.valueOf(mSlotId);
    }

    @Override
    public String getNetworkSDKVersion() {
        return MyTargetATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return MyTargetATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {
        return mRewardedAd != null && mIsReady;
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return MyTargetATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getBiddingToken(Context context) {
        return MyTargetManager.getBidderToken(context);
    }
}
