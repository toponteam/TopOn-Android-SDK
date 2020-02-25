package com.anythink.nativead.bussiness.utils;

import android.content.Context;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.CommonDeviceUtil;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;
import com.anythink.nativead.unitgroup.api.CustomNativeAdapter;
import com.anythink.nativead.unitgroup.api.CustomNativeListener;

import java.util.Map;

/**
 * Created by Z on 2018/1/9.
 */

public final class CustomNativeAdapterParser {
    private CustomNativeAdapterParser() {
    }


    public static void loadNativeAd(final Context context, final CustomNativeAdapter customEventNativeAdapter,
                                    final PlaceStrategy placeStrategy,
                                    final PlaceStrategy.UnitGroupInfo unitGroupInfo,
                                    final Map<String, Object> serviceExtras,
                                    final Map<String, Object> localExtras,
                                    final CustomNativeListener listener) {

        try {
            boolean isAutoPlay = CommonDeviceUtil.getNetwork(context) == Const.NET_TYPE_WIFI && placeStrategy.getWifiAutoSw() == 1;
            serviceExtras.put(CustomNativeAd.IS_AUTO_PLAY_KEY, isAutoPlay);
            serviceExtras.put(CustomNativeAd.AD_REQUEST_NUM, unitGroupInfo.getUnitAdRequestNumber());

        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }

        // Custom event classes can be developed by any third party and may not be tested.
        // We catch all exceptions here to prevent crashes from untested code.

        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    customEventNativeAdapter.loadNativeAd(
                            context,
                            listener,
                            serviceExtras,
                            localExtras);
                } catch (Throwable e) {
                    e.printStackTrace();
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.adapterInnerError, "", e.getMessage());
                    listener.onNativeAdFailed(customEventNativeAdapter, adError);
                }
            }
        });

    }


}