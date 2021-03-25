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
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;

import java.util.Map;

public class IronsourceATInterstitialAdapter extends CustomInterstitialAdapter {
    private final String TAG = IronsourceATInterstitialAdapter.class.getSimpleName();

    String instanceId = "";

    /***
     * init and load
     */
    private void initAndLoad(Activity activity, Map<String, Object> serverExtras) {
        if (ATSDK.isNetworkLogDebug()) {
            IntegrationHelper.validateIntegration(activity);
        }


        IronsourceATInitManager.getInstance().initSDK(activity, serverExtras, new IronsourceATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                try {
                    if (IronSource.isISDemandOnlyInterstitialReady(instanceId)) {
                        mLoadListener.onAdCacheLoaded();
                    } else {
                        IronsourceATInitManager.getInstance().loadInterstitial(instanceId, IronsourceATInterstitialAdapter.this);
                    }
                } catch (Throwable e) {
                    if (mLoadListener != null) {
                        mLoadListener.onAdLoadError("", e.getMessage());
                    }
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
                mLoadListener.onAdLoadError("", "Ironsource activity must be activity.");
            }
            return;
        }
        initAndLoad((Activity) context, serverExtras);
    }

    @Override
    public boolean isAdReady() {
        return IronSource.isISDemandOnlyInterstitialReady(instanceId);
    }

    @Override
    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return IronsourceATInitManager.getInstance().setUserDataConsent(context, isConsent, isEUTraffic);
    }

    @Override
    public String getNetworkSDKVersion() {
        return IronsourceATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public void show(Activity activity) {
        IronsourceATInitManager.getInstance().putAdapter("inter_" + instanceId, this);
        IronSource.showISDemandOnlyInterstitial(instanceId);
    }

    @Override
    public void destory() {
        IronSource.clearRewardedVideoServerParameters();
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

    protected void onInterstitialAdReady() {
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }
    }

    protected void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(ironSourceError.getErrorCode() + "", ironSourceError.getErrorMessage());
        }
    }

    protected void onInterstitialAdOpened() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow();
        }

    }

    protected void onInterstitialAdClosed() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose();
        }
    }


    protected void onInterstitialAdClicked() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked();
        }
    }

}