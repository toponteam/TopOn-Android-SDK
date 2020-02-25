package com.anythink.network.sigmob;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.anythink.splashad.unitgroup.api.CustomSplashListener;
import com.sigmob.windad.Splash.WindSplashAD;
import com.sigmob.windad.Splash.WindSplashADListener;
import com.sigmob.windad.Splash.WindSplashAdRequest;
import com.sigmob.windad.WindAdError;

import java.util.Map;

public class SigmobATSplashAdapter extends CustomSplashAdapter implements WindSplashADListener {

    private static final String TAG = SigmobATSplashAdapter.class.getSimpleName();
    private CustomSplashListener mListener;
    private String mPlacementId = "";

    @Override
    public void loadSplashAd(final Activity activity, final ViewGroup constainer, View skipView, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomSplashListener customSplashListener) {
        mListener = customSplashListener;

        String appId = "";
        String appKey = "";
        if (serverExtras == null) {
            if (mListener != null) {
                mListener.onSplashAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "service params is empty."));
            }
            return;
        } else {
            if (serverExtras.containsKey("app_id")) {
                appId = serverExtras.get("app_id").toString();
            }
            if (serverExtras.containsKey("app_key")) {
                appKey = serverExtras.get("app_key").toString();
            }
            if (serverExtras.containsKey("placement_id")) {
                mPlacementId = serverExtras.get("placement_id").toString();
            }

            if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey) || TextUtils.isEmpty(mPlacementId)) {
                if (mListener != null) {
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "app_id、app_key、placement_id could not be null.");
                    mListener.onSplashAdFailed(this, adError);
                }
                return;
            }
        }

        SigmobATInitManager.getInstance().initSDK(activity, serverExtras, new SigmobATInitManager.InitCallback() {
            @Override
            public void onFinish() {
                CommonLogUtil.d(TAG, "load()...");
                WindSplashAdRequest splashAdRequest = new WindSplashAdRequest(mPlacementId,"", null);

                //show ad
                new WindSplashAD(activity, constainer ,splashAdRequest, SigmobATSplashAdapter.this);
            }
        });
    }

    @Override
    public String getSDKVersion() {
        return SigmobATConst.getSDKVersion();
    }

    @Override
    public void clean() {

    }

    @Override
    public String getNetworkName() {
        return SigmobATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void onSplashAdSuccessPresentScreen() {
        if(mListener != null) {
            mListener.onSplashAdLoaded(this);
            mListener.onSplashAdShow(this);
        }
    }

    @Override
    public void onSplashAdFailToPresent(WindAdError windAdError, String s) {
        if(mListener != null) {
            mListener.onSplashAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, "" + windAdError.getErrorCode(), windAdError.toString()));
        }
    }

    @Override
    public void onSplashAdClicked() {
        if(mListener != null) {
            mListener.onSplashAdClicked(this);
        }
    }

    @Override
    public void onSplashClosed() {
        CommonLogUtil.d(TAG, "onSplashClosed()");
    }
}
