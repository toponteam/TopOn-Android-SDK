/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.ironsource;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATSDK;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;

import java.util.Map;



public class IronsourceATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    String instanceId = "";

    /***
     * init and load
     */
    private void initAndLoad(final Activity activity, Map<String, Object> serverExtras) {
        if (ATSDK.isNetworkLogDebug()) {
            IntegrationHelper.validateIntegration(activity);
        }


        IronSource.setUserId(mUserId);
        IronSource.setDynamicUserId(mUserId);

        IronsourceATInitManager.getInstance().initSDK(activity, serverExtras, new IronsourceATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                try {
                    if (IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId)) {
                        mLoadListener.onAdCacheLoaded();
                    } else {
                        IronsourceATInitManager.getInstance().loadRewardedVideo(instanceId, IronsourceATRewardedVideoAdapter.this);
                    }
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
                }
                try {
                    if (activity != null) {
                        IronSource.onResume(activity);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void loadCustomNetworkAd(Context context, Map<String, Object> serverExtras, Map<String, Object> localExtras) {

        String appkey = (String) serverExtras.get("app_key");
        instanceId = (String) serverExtras.get("instance_id");

        if (TextUtils.isEmpty(appkey) || TextUtils.isEmpty(instanceId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "ironsource app_key or instance_id is empty.");
            }
            return;
        }

        if (!(context instanceof Activity)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "Ironsource context must be activity.");
            }
            return;
        }


        initAndLoad(((Activity) context), serverExtras);
    }

    @Override
    public boolean isAdReady() {
        return IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return IronsourceATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            IronsourceATInitManager.getInstance().putAdapter("rv_" + instanceId, this);
            IronSource.showISDemandOnlyRewardedVideo(instanceId);
        }

    }

    @Override
    public void destory() {
        IronSource.clearRewardedVideoServerParameters();
    }


    @Override
    public String getNetworkSDKVersion() {
        return IronsourceATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public String getNetworkName() {
        return IronsourceATInitManager.getInstance().getNetworkName();
    }

    @Override
    public String getNetworkPlacementId() {
        return instanceId;
    }

    /**
     * -------------------------------------------callback-------------------------------------------------------
     **/
    public void onRewardedVideoAdOpened() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayStart();
        }
    }

    public void onRewardedVideoAdClosed() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd();
            mImpressionListener.onRewardedVideoAdClosed();
        }
        try {
            if (mActivityRef.get() != null) {
                IronSource.onPause(mActivityRef.get());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void onRewardedVideoAdLoadSuccess() {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }
    }

    public void onRewardedVideoAdLoadFailed(IronSourceError ironSourceError) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(ironSourceError.getErrorCode() + "", ironSourceError.getErrorMessage());
        }
    }

    public void onRewardedVideoAdRewarded() {
        if (mImpressionListener != null) {
            mImpressionListener.onReward();
        }
    }

    public void onRewardedVideoAdShowFailed(IronSourceError pIronSourceError) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayFailed("" + pIronSourceError.getErrorCode(), " " + pIronSourceError.getErrorMessage());
        }
    }

    public void onRewardedVideoAdClicked() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked();
        }
    }

}