package com.anythink.rewardvideo.bussiness.utils;

import android.app.Activity;

import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.api.AdError;
import com.anythink.core.api.ErrorCode;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoListener;

import java.util.Map;

/**
 * Created by Z on 2018/1/9.
 */

public final class CustomRewardVideoAdapterParser {
    private CustomRewardVideoAdapterParser() {
    }

    public static void loadRewardVideoAd(final Activity activity, final CustomRewardVideoAdapter customRewardVideoAdapter,
                                         final PlaceStrategy.UnitGroupInfo unitGroupInfo,
                                         final Map<String, Object> serviceExtras,
                                         final ATMediationSetting setting,
                                         final CustomRewardVideoListener customListener) {

        // Custom event classes can be developed by any third party and may not be tested.
        // We catch all exceptions here to prevent crashes from untested code.

        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                try {
                    customRewardVideoAdapter.loadRewardVideoAd(
                            activity,
                            serviceExtras,
                            setting, customListener);
                } catch (Throwable e) {
                    e.printStackTrace();
                    AdError adError = ErrorCode.getErrorCode(ErrorCode.adapterInnerError, "", e.getMessage());
                    customListener.onRewardedVideoAdFailed(customRewardVideoAdapter, adError);
                }
            }
        });

    }


}