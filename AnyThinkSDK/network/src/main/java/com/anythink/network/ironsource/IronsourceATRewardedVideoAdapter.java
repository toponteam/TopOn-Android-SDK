package com.anythink.network.ironsource;

import android.app.Activity;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ErrorCode;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */


public class IronsourceATRewardedVideoAdapter extends CustomRewardVideoAdapter {

    IronsourceRewardedVideoSetting mIronsourceMediationSetting;
    String instanceId = "";

    /***
     * init and load
     */
    private void initAndLoad(final Activity activity, Map<String, Object> serverExtras) {
        IntegrationHelper.validateIntegration(activity);

        IronSource.setUserId(mUserId);
        IronSource.setDynamicUserId(mUserId);

        IronsourceATInitManager.getInstance().initSDK(activity, serverExtras, new IronsourceATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                if (IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId)) {
                    mLoadResultListener.onRewardedVideoAdLoaded(IronsourceATRewardedVideoAdapter.this);
                } else {
                    IronsourceATInitManager.getInstance().loadRewardedVideo(instanceId, IronsourceATRewardedVideoAdapter.this);
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
    public void loadRewardVideoAd(Activity activity, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomRewardVideoListener customRewardVideoListener) {
        mLoadResultListener = customRewardVideoListener;
        if (activity == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }
        if (mediationSetting != null && mediationSetting instanceof IronsourceRewardedVideoSetting) {
            mIronsourceMediationSetting = (IronsourceRewardedVideoSetting) mediationSetting;
        }


        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "This placement's params in server is null!"));
            }
            return;
        } else {

            String appkey = (String) serverExtras.get("app_key");
            instanceId = (String) serverExtras.get("instance_id");

            if (TextUtils.isEmpty(appkey) || TextUtils.isEmpty(instanceId)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onRewardedVideoAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "ironsource app_key or instance_id is empty."));
                }
                return;
            }
        }
        initAndLoad(activity, serverExtras);
    }

    @Override
    public boolean isAdReady() {
        return IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId);
    }

    @Override
    public void show(Activity activity) {
        if (isAdReady()) {
            IronsourceATInitManager.getInstance().putAdapter("rv_" + instanceId, this);
            IronSource.showISDemandOnlyRewardedVideo(instanceId);
        }

    }

    @Override
    public void clean() {
        IronSource.clearRewardedVideoServerParameters();
    }

    @Override
    public void onResume(Activity activity) {
    }

    @Override
    public void onPause(Activity activity) {
    }

    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public String getNetworkName() {
        return IronsourceATInitManager.getInstance().getNetworkName();
    }

    /**
     * -------------------------------------------callback-------------------------------------------------------
     **/
    public void onRewardedVideoAdOpened() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayStart(IronsourceATRewardedVideoAdapter.this);
        }
    }

    public void onRewardedVideoAdClosed() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayEnd(IronsourceATRewardedVideoAdapter.this);
            mImpressionListener.onRewardedVideoAdClosed(IronsourceATRewardedVideoAdapter.this);
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
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdLoaded(IronsourceATRewardedVideoAdapter.this);
        }
    }

    public void onRewardedVideoAdLoadFailed(IronSourceError ironSourceError) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onRewardedVideoAdFailed(IronsourceATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, ironSourceError.getErrorCode() + "", ironSourceError.getErrorMessage()));
        }
    }

    public void onRewardedVideoAdRewarded() {
        if (mImpressionListener != null) {
            mImpressionListener.onReward(IronsourceATRewardedVideoAdapter.this);
        }
    }

    public void onRewardedVideoAdShowFailed(IronSourceError pIronSourceError) {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayFailed(IronsourceATRewardedVideoAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + pIronSourceError.getErrorCode(), " " + pIronSourceError.getErrorMessage()));
        }
    }

    public void onRewardedVideoAdClicked() {
        if (mImpressionListener != null) {
            mImpressionListener.onRewardedVideoAdPlayClicked(IronsourceATRewardedVideoAdapter.this);
        }
    }

}