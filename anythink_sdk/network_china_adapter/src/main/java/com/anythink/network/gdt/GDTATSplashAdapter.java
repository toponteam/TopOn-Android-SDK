package com.anythink.network.gdt;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.anythink.splashad.unitgroup.api.CustomSplashListener;

import java.util.Map;

public class GDTATSplashAdapter extends CustomSplashAdapter implements SplashADListener {

    private String mUnitId;

    CustomSplashListener mListener;

    @Override
    public void loadSplashAd(final Activity activity, final ViewGroup container, View skipView, Map<String, Object> serverExtras, ATMediationSetting mediationSetting, CustomSplashListener customSplashListener) {
        String appid = "";
        String unitId = "";

        mListener = customSplashListener;
        if (serverExtras.containsKey("app_id")) {
            appid = serverExtras.get("app_id").toString();
        }
        if (serverExtras.containsKey("unit_id")) {
            unitId = serverExtras.get("unit_id").toString();
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId)) {
            if (mListener != null) {
                AdError adError = ErrorCode.getErrorCode(ErrorCode.noADError, "", "GTD appid or unitId is empty.");
                mListener.onSplashAdFailed(this, adError);

            }
            return;
        }

        mUnitId = unitId;

        GDTATInitManager.getInstance().initSDK(activity, serverExtras, new GDTATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                SplashAD splashAD = new SplashAD(activity, mUnitId, GDTATSplashAdapter.this, 5000);
                splashAD.fetchAndShowIn(container);
            }

            @Override
            public void onError() {
                if (mListener != null) {
                    mListener.onSplashAdFailed(GDTATSplashAdapter.this, ErrorCode.getErrorCode(ErrorCode.noADError, "", "GDT initSDK failed."));
                }
            }
        });
    }

    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void clean() {

    }

    @Override
    public void onADDismissed() {
        if (mListener != null) {
            mListener.onSplashAdDismiss(this);
        }
    }

    @Override
    public void onNoAD(com.qq.e.comm.util.AdError adError) {
        if (mListener != null) {
            mListener.onSplashAdFailed(this, ErrorCode.getErrorCode(ErrorCode.noADError, adError.getErrorCode() + "", adError.getErrorMsg()));
        }
    }

    @Override
    public void onADPresent() {
        if (mListener != null) {
            mListener.onSplashAdShow(this);
        }
    }

    @Override
    public void onADClicked() {
        if (mListener != null) {
            mListener.onSplashAdClicked(this);
        }
    }

    @Override
    public void onADTick(long l) {

    }

    @Override
    public void onADExposure() {
    }

    @Override
    public void onADLoaded(long l) {
        if (mListener != null) {
            mListener.onSplashAdLoaded(this);
        }
    }

    @Override
    public String getSDKVersion() {
        return GDTATConst.getNetworkVersion();
    }
}
