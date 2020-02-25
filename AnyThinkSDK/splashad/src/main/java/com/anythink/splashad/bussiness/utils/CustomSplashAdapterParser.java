package com.anythink.splashad.bussiness.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;
import com.anythink.splashad.unitgroup.api.CustomSplashListener;

import java.util.Map;

/**
 * Created by Z on 2018/1/9.
 */

public final class CustomSplashAdapterParser {
    private CustomSplashAdapterParser() {
    }

    public static void loadSplashAd(final Activity activity, final ViewGroup constainer, final View skipView, final CustomSplashAdapter customSplashAdapter,
                                    final PlaceStrategy.UnitGroupInfo unitGroupInfo,
                                    final Map<String, Object> serviceExtras,
                                    final ATMediationSetting setting,
                                    final CustomSplashListener customListener) {

        // Custom event classes can be developed by any third party and may not be tested.
        // We catch all exceptions here to prevent crashes from untested code.

        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    customSplashAdapter.loadSplashAd(
                            activity,
                            constainer, skipView,
                            serviceExtras,
                            setting, customListener);
                } catch (Throwable e) {
                    e.printStackTrace();
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.adapterInnerError, "", e.getMessage());
                    customListener.onSplashAdFailed(customSplashAdapter, adError);
                }
            }
        });

    }


}