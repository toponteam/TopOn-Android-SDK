package com.anythink.banner.business.utils;

import android.content.Context;

import com.anythink.banner.api.ATBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.banner.unitgroup.api.CustomBannerListener;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.Map;

/**
 * Created by Z on 2018/1/9.
 * BannerAdapter parser
 */

public final class CustomBannerAdapterParser {
    private CustomBannerAdapterParser() {
    }

    public static CustomBannerAdapter createBannerAdapter(final PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        CustomBannerAdapter customBannerAdapter;

        try {
            customBannerAdapter = CustomBannerFactory.create(unitGroupInfo.adapterClassName);
        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
        return customBannerAdapter;
    }

    public static void loadBannerAd(final ATBannerView bannerView, final Context activity, final CustomBannerAdapter customBannerAdapter,
                                    final PlaceStrategy.UnitGroupInfo unitGroupInfo,
                                    final Map<String, Object> serviceExtras,
                                    final ATMediationSetting setting,
                                    final CustomBannerListener customListener) {

        // Custom event classes can be developed by any third party and may not be tested.
        // We catch all exceptions here to prevent crashes from untested code.

        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    customBannerAdapter.loadBannerAd(bannerView,
                            activity,
                            serviceExtras,
                            setting, customListener);
                } catch (Throwable e) {
                    e.printStackTrace();
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.adapterInnerError, "", e.getMessage());
                    customListener.onBannerAdLoadFail(customBannerAdapter, adError);
                }
            }
        });

    }


}