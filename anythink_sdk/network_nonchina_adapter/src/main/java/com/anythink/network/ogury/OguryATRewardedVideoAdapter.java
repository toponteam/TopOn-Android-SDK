/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ogury;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.util.Map;

import io.presage.common.AdConfig;
import io.presage.common.network.models.RewardItem;
import io.presage.interstitial.optinvideo.PresageOptinVideo;
import io.presage.interstitial.optinvideo.PresageOptinVideoCallback;

public class OguryATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    String mUnitId;
    boolean mIsReward;

    private PresageOptinVideo mPresageOptinVideo;

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String assetKey = "";
        String unitId = "";
        if (serverExtras.containsKey("key")) {
            assetKey = serverExtras.get("key").toString();
        }
        if (serverExtras.containsKey("unit_id")) {
            unitId = serverExtras.get("unit_id").toString();
        }

        if (TextUtils.isEmpty(assetKey) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "asset_key、unit_id could not be null.");
            }
            return;
        }
        mUnitId = unitId;

        OguryATInitManager.getInstance().initSDK(context, serverExtras, new OguryATInitManager.Callback() {
            @Override
            public void onSuccess() {
                init(context);
            }
        });
    }

    private void init(Context context) {
        AdConfig adConfig = new AdConfig(mUnitId);
        mPresageOptinVideo = new PresageOptinVideo(context.getApplicationContext(), adConfig);
        if (!TextUtils.isEmpty(mUserId)) {
            mPresageOptinVideo.setUserId(mUserId);
        }
        mPresageOptinVideo.setOptinVideoCallback(new PresageOptinVideoCallback() {
            @Override
            public void onAdAvailable() {
                if (mLoadListener != null) {
                    mLoadListener.onAdDataLoaded();
                }
            }

            @Override
            public void onAdNotAvailable() {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "onAdNotAvailable");
                }
            }

            @Override
            public void onAdLoaded() {
                if (mLoadListener != null) {
                    mLoadListener.onAdCacheLoaded();
                }
            }

            @Override
            public void onAdNotLoaded() {
            }

            @Override
            public void onAdDisplayed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayStart();
                }
            }

            @Override
            public void onAdClosed() {
                if (mImpressionListener != null) {
                    mImpressionListener.onRewardedVideoAdPlayEnd();
                    if (mIsReward) {
                        mImpressionListener.onReward();
                    }
                    mImpressionListener.onRewardedVideoAdClosed();
                }
            }

            @Override
            public void onAdError(int code) {
                /*
                code 0: load failed
                code 1: phone not connected to internet
                code 2: ad disabled
                code 3: various error (configuration file not synced)
                code 4: ad expires in 4 hours if it was not shown
                code 5: start method not called
                */
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("" + code, OguryATInitManager.getErrorMsg(code));
                }
            }

            @Override
            public void onAdRewarded(RewardItem rewardItem) {
                mIsReward = true;
            }
        });

        mPresageOptinVideo.load();
    }

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            mPresageOptinVideo.show();
        }
    }


    @Override
    public boolean isAdReady() {
        return mPresageOptinVideo != null && mPresageOptinVideo.isLoaded();
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    @Override
    public String getNetworkSDKVersion() {
        return OguryATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void destory() {
        if (mPresageOptinVideo != null) {
            mPresageOptinVideo.setOptinVideoCallback(null);
            mPresageOptinVideo = null;
        }
    }

    @Override
    public String getNetworkName() {
        return OguryATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }
}
