package com.anythink.network.ironsource;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.ErrorCode;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;

import java.util.Map;

/**
 * Created by zhou on 2018/6/27.
 */
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
                if (IronSource.isISDemandOnlyInterstitialReady(instanceId)) {
                    mLoadResultListener.onInterstitialAdLoaded(IronsourceATInterstitialAdapter.this);
                } else {
                    IronsourceATInitManager.getInstance().loadInterstitial(instanceId, IronsourceATInterstitialAdapter.this);
                }
            }
        });
    }

    @Override
    public void loadInterstitialAd(Context context, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomInterstitialListener customInterstitialListener) {
        mLoadResultListener = customInterstitialListener;
        if (context == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity is null."));
            }
            return;
        }

        if (serverExtras == null) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "ironsource this placement's params in server is null!"));
            }
            return;
        } else {

            String appkey = (String) serverExtras.get("app_key");
            instanceId = (String) serverExtras.get("instance_id");

            if (TextUtils.isEmpty(appkey) || TextUtils.isEmpty(instanceId)) {
                if (mLoadResultListener != null) {
                    mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "ironsource app_key or instance_id is empty."));
                }
                return;
            }
        }

        if (!(context instanceof Activity)) {
            if (mLoadResultListener != null) {
                mLoadResultListener.onInterstitialAdLoadFail(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "activity must be activity."));
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
    public String getSDKVersion() {
        return "";
    }

    @Override
    public void show(Context context) {
        if (isAdReady()) {
            IronsourceATInitManager.getInstance().putAdapter("inter_" + instanceId, this);
            IronSource.showISDemandOnlyInterstitial(instanceId);
        }

    }

    @Override
    public void clean() {
        IronSource.clearRewardedVideoServerParameters();
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public String getNetworkName() {
        return IronsourceATInitManager.getInstance().getNetworkName();
    }

    /**
     * -------------------------------------------callback-------------------------------------------------------
     **/

    protected void onInterstitialAdReady() {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoaded(IronsourceATInterstitialAdapter.this);
        }
    }

    protected void onInterstitialAdLoadFailed(IronSourceError ironSourceError) {
        if (mLoadResultListener != null) {
            mLoadResultListener.onInterstitialAdLoadFail(IronsourceATInterstitialAdapter.this
                    , ErrorCode.getErrorCode(ErrorCode.noADError, ironSourceError.getErrorCode() + "", ironSourceError.getErrorMessage()));
        }
    }

    protected void onInterstitialAdOpened() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdShow(IronsourceATInterstitialAdapter.this);
        }

    }

    protected void onInterstitialAdClosed() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClose(IronsourceATInterstitialAdapter.this);
        }
    }


    protected void onInterstitialAdClicked() {
        if (mImpressListener != null) {
            mImpressListener.onInterstitialAdClicked(IronsourceATInterstitialAdapter.this);
        }
    }

}