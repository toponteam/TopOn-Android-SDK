package com.anythink.interstitial.business.utils;

import android.content.Context;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialListener;

import java.util.Map;

/**
 * Created by Z on 2018/1/9.
 * 通过反射拿到Adapter对象来广告加载
 */

public final class CustomInterstitialAdapterParser {
    private CustomInterstitialAdapterParser() {
    }

    public static CustomInterstitialAdapter createInterstitialAdapter(final PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        CustomInterstitialAdapter customInterstitialAdapter;
        try {
            customInterstitialAdapter = CustomInterstitialFactory.create(unitGroupInfo.adapterClassName);
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
        return customInterstitialAdapter;
    }

    public static void loadInterstitialAd(final Context activity, final CustomInterstitialAdapter customInterstitialAdapter,
                                          final PlaceStrategy.UnitGroupInfo unitGroupInfo,
                                          final Map<String, Object> serviceExtras,
                                          final ATMediationSetting setting,
                                          final CustomInterstitialListener customListener) {

        // Custom event classes can be developed by any third party and may not be tested.
        // We catch all exceptions here to prevent crashes from untested code.

        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    customInterstitialAdapter.loadInterstitialAd(
                            activity,
                            serviceExtras,
                            setting, customListener);
                } catch (Throwable e) {
                    e.printStackTrace();
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.adapterInnerError, "", e.getMessage());
                    customListener.onInterstitialAdLoadFail(customInterstitialAdapter, adError);
                }
            }
        });

    }


}