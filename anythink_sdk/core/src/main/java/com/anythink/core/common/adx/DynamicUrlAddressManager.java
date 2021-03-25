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
import com.anythink.core.common.entity.DynamicUrlSettings;
import com.anythink.core.strategy.AppStrategy;
import com.anythink.core.strategy.AppStrategyManager;

public class DynamicUrlAddressManager {

    private static DynamicUrlAddressManager sIntance;

    private DynamicUrlAddressManager() {

    }

    public synchronized static DynamicUrlAddressManager getInstance() {
        if (sIntance == null) {
            sIntance = new DynamicUrlAddressManager();
        }
        return sIntance;
    }

    public String getBidRequestUrl() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        DynamicUrlSettings adxApiUrlSetting = appStrategy.getDynamicUrlSettings();
        if (adxApiUrlSetting == null || TextUtils.isEmpty(adxApiUrlSetting.getAdxBidRequestHttpUrl())) {
            return Const.API.URL_HEADBIDDING;
        }

        return adxApiUrlSetting.getAdxBidRequestHttpUrl();
    }

    public String getAdxOfferRequestUrl() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        DynamicUrlSettings adxApiUrlSetting = appStrategy.getDynamicUrlSettings();
        if (adxApiUrlSetting == null || TextUtils.isEmpty(adxApiUrlSetting.getAdxRequestHttpUrl())) {
            return Const.API.URL_ADX_REQUEST;
        }

        return adxApiUrlSetting.getAdxRequestHttpUrl();
    }


    public String getAdxTrackRequestUrl() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        DynamicUrlSettings adxApiUrlSetting = appStrategy.getDynamicUrlSettings();
        if (adxApiUrlSetting == null || TextUtils.isEmpty(adxApiUrlSetting.getAdxTrackRequestHttpUrl())) {
            return Const.API.URL_ADX_TK;
        }

        return adxApiUrlSetting.getAdxTrackRequestHttpUrl();
    }

    public String getOnlineApiRequestUrl() {
        AppStrategy appStrategy = AppStrategyManager.getInstance(SDKContext.getInstance().getContext()).getAppStrategyByAppId(SDKContext.getInstance().getAppId());
        DynamicUrlSettings adxApiUrlSetting = appStrategy.getDynamicUrlSettings();
        if (adxApiUrlSetting == null || TextUtils.isEmpty(adxApiUrlSetting.getOnlineApiRequestHttpUrl())) {
            return Const.API.URL_ONLINE_API_REQUEST;
        }

        return adxApiUrlSetting.getOnlineApiRequestHttpUrl();
    }
}
