/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

import android.content.Context;

import com.anythink.core.strategy.AppStrategy;

import org.json.JSONObject;

public interface IATChinaSDKHandler {
    void initDeviceInfo(Context context);

    void fillRequestData(JSONObject jsonObject, AppStrategy appStrategy);
}
