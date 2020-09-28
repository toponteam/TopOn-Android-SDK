package com.anythink.network.gdt;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;

import java.util.Map;

public class GDTATSplashAdapter extends CustomSplashAdapter implements SplashADListener {

    private String mUnitId;

    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        String appid = "";
        String unitId = "";

        if (serverExtra.containsKey("app_id")) {
            appid = serverExtra.get("app_id").toString();
        }
        if (serverExtra.containsKey("unit_id")) {
            unitId = serverExtra.get("unit_id").toString();
        }

        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(unitId)) {
            if (mLoadListener != null) {
                mLoadListener.onAdLoadError("", "GTD appid or unitId is empty.");

            }
            return;
        }

        mUnitId = unitId;

        GDTATInitManager.getInstance().initSDK(context, serverExtra, new GDTATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                SplashAD splashAD = new SplashAD(((Activity) context), mUnitId, GDTATSplashAdapter.this, 5000);
                splashAD.fetchAndShowIn(mContainer);
            }

            @Override
            public void onError() {
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadError("", "GDT initSDK failed.");
                }
            }
        });
    }

    @Override
    public void destory() {

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
    public void onADDismissed() {
        if (mImpressionListener != null) {
            mImpressionListener.onSplashAdDismiss();
        }
    }

    @Override
    public void onNoAD(com.qq.e.comm.util.AdError adError) {
        if (mLoadListener != null) {
            mLoadListener.onAdLoadError(adError.getErrorCode() + "", adError.getErrorMsg());
        }
    }

    @Override
    public void onADPresent() {
        if (mImpressionListener != null) {
            mImpressionListener.onSplashAdShow();
        }
    }

    @Override
    public void onADClicked() {
        if (mImpressionListener != null) {
            mImpressionListener.onSplashAdClicked();
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
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }
    }

}
