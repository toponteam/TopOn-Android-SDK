/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.adx;

import android.text.TextUtils;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdxApiUrlSetting;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

public class AdxUrlAddressManager {

    private static AdxUrlAddressManager sIntance;

    private AdxUrlAddressManager() {

    }

    public synchronized static AdxUrlAddressManager getInstance() {
        if (sIntance == null) {
            sIntance = new AdxUrlAddressManager();
        }
        return sIntance;
    }

    public String getBidRequestUrl() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        AdxApiUrlSetting adxApiUrlSetting = appStrategy.getAdxSetting();
        if (adxApiUrlSetting == null || TextUtils.isEmpty(adxApiUrlSetting.getAdxBidRequestHttpUrl())) {
            return Const.API.URL_HEADBIDDING;
        }

        return adxApiUrlSetting.getAdxBidRequestHttpUrl();
    }

    public String getAdxOfferRequestUrl() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        AdxApiUrlSetting adxApiUrlSetting = appStrategy.getAdxSetting();
        if (adxApiUrlSetting == null || TextUtils.isEmpty(adxApiUrlSetting.getAdxRequestHttpUrl())) {
            return Const.API.URL_ADX_REQUEST;
        }

        return adxApiUrlSetting.getAdxRequestHttpUrl();
    }


    public String getAdxTrackRequestUrl() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        AdxApiUrlSetting adxApiUrlSetting = appStrategy.getAdxSetting();
        if (adxApiUrlSetting == null || TextUtils.isEmpty(adxApiUrlSetting.getAdxTrackRequestHttpUrl())) {
            return Const.API.URL_ADX_TK;
        }

        return adxApiUrlSetting.getAdxTrackRequestHttpUrl();
    }
}
