/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

import android.content.Context;

import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.strategy.AppStrategy;

import org.json.JSONObject;

public interface IExHandler {
    void initDeviceInfo(Context context);

    void fillRequestData(JSONObject jsonObject, AppStrategy appStrategy);

    void handleOfferClick(Context context, BaseAdRequestInfo baseAdRequestInfo, BaseAdContent baseAdContent, String url, String clickId, Runnable callbackRunnable);
}
