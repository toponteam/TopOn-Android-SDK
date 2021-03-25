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
import android.view.ViewGroup;

import com.anythink.core.api.ATAdConst;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.compliance.DownloadConfirmCallBack;
import com.qq.e.comm.compliance.DownloadConfirmListener;

import java.util.Map;

public class GDTATSplashAdapter extends CustomSplashAdapter implements SplashADListener {

    private String mUnitId;
    private boolean isReady;

    private SplashAD splashAD;

    private boolean isUseDownloadDialogFrame;

    @Override
    public String getNetworkName() {
        return GDTATInitManager.getInstance().getNetworkName();
    }

    @Override
    public boolean isAdReady() {
        return isReady;
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

        isReady = false;

        isUseDownloadDialogFrame = false;
        try {
            if (localExtra != null && localExtra.containsKey(ATAdConst.KEY.AD_CLICK_CONFIRM_STATUS)) {
                isUseDownloadDialogFrame = Boolean.parseBoolean(localExtra.get(ATAdConst.KEY.AD_CLICK_CONFIRM_STATUS).toString());
            }
        } catch (Exception e) {

        }

        GDTATInitManager.getInstance().initSDK(context, serverExtra, new GDTATInitManager.OnInitCallback() {
            @Override
            public void onSuccess() {
                splashAD = new SplashAD(context, mUnitId, GDTATSplashAdapter.this, mFetchAdTimeout);
                splashAD.fetchAdOnly();
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
    public void show(Activity activity, ViewGroup container) {
        if (isReady && splashAD != null) {
            splashAD.showAd(container);
        }
    }

    @Override
    public void destory() {
        splashAD = null;
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
        if (mImpressionListener != null) {
            mImpressionListener.onSplashAdShow();
        }
    }

    @Override
    public void onADLoaded(long l) {
        isReady = true;
        if (splashAD != null && isUseDownloadDialogFrame) {
            splashAD.setDownloadConfirmListener(new DownloadConfirmListener() {
                @Override
                public void onDownloadConfirm(Activity activity, int i, String s, DownloadConfirmCallBack downloadConfirmCallBack) {
                    if (mImpressionListener != null) {
                        GDTDownloadFirmInfo gdtDownloadFirmInfo = new GDTDownloadFirmInfo();
                        gdtDownloadFirmInfo.appInfoUrl = s;
                        gdtDownloadFirmInfo.scenes = i;
                        gdtDownloadFirmInfo.confirmCallBack = downloadConfirmCallBack;
                        mImpressionListener.onDownloadConfirm(activity, gdtDownloadFirmInfo);
                    }
                }
            });
        }
        if (mLoadListener != null) {
            mLoadListener.onAdCacheLoaded();
        }
    }


}
