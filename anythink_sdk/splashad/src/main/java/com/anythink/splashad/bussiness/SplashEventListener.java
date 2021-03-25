/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.splashad.bussiness;

import android.content.Context;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATNetworkConfirmInfo;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.net.TrackingV2Loader;
import com.anythink.core.common.track.AdTrackingManager;
import com.anythink.core.common.utils.CommonSDKUtil;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.anythink.splashad.unitgroup.api.CustomSplashEventListener;

public class SplashEventListener implements CustomSplashEventListener {
    CustomSplashAdapter splashAdapter;
    AdEventListener splashAdListener;

    public SplashEventListener(CustomSplashAdapter splashAdapter, AdEventListener splashAdListener) {
        this.splashAdapter = splashAdapter;
        this.splashAdListener = splashAdListener;
    }

    @Override
    public void onSplashAdShow() {
        if (splashAdapter != null) {
            AdTrackingInfo adTrackingInfo = splashAdapter.getTrackingInfo();

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_SHOW_TYPE, adTrackingInfo);

            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.IMPRESSION, Const.LOGKEY.SUCCESS, "");

        }

        if (splashAdListener != null) {
            splashAdListener.onAdShow(ATAdInfo.fromAdapter(splashAdapter));
        }

    }

    @Override
    public void onSplashAdClicked() {
        if (splashAdapter != null) {
            AdTrackingInfo adTrackingInfo = splashAdapter.getTrackingInfo();

            AdTrackingManager.getInstance(SDKContext.getInstance().getContext()).addAdTrackingInfo(TrackingV2Loader.AD_CLICK_TYPE, adTrackingInfo);

            CommonSDKUtil.printAdTrackingInfoStatusLog(adTrackingInfo, Const.LOGKEY.CLICK, Const.LOGKEY.SUCCESS, "");

        }

        if (splashAdListener != null) {
            splashAdListener.onAdClick(ATAdInfo.fromAdapter(splashAdapter));
        }
    }

    @Override
    public void onSplashAdDismiss() {
        if (splashAdapter != null && splashAdapter.getTrackingInfo() != null) {
            CommonSDKUtil.printAdTrackingInfoStatusLog(splashAdapter.getTrackingInfo(), Const.LOGKEY.CLOSE, Const.LOGKEY.SUCCESS, "");
        }

        if (splashAdListener != null) {
            splashAdListener.onCallbackAdDismiss(ATAdInfo.fromAdapter(splashAdapter));
        }

        if (splashAdapter != null) {
            splashAdapter.cleanImpressionListener();
        }

        if (splashAdapter != null) {
            splashAdapter.destory();
        }

        splashAdListener = null;
    }

    @Override
    public void onDeeplinkCallback(boolean isSuccess) {
        if (splashAdListener != null) {
            splashAdListener.onDeeplinkCallback(ATAdInfo.fromAdapter(splashAdapter), isSuccess);
        }
    }

    @Override
    public void onDownloadConfirm(Context context, ATNetworkConfirmInfo networkConfirmInfo) {
        if (splashAdListener != null) {
            splashAdListener.onDownloadConfirm(context, ATAdInfo.fromAdapter(splashAdapter), networkConfirmInfo);
        }
    }
}
