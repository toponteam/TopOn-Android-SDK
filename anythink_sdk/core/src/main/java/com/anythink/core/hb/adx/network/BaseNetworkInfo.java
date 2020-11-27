/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb.adx.network;

import org.json.JSONObject;

public abstract class BaseNetworkInfo {
    protected String format;

    public BaseNetworkInfo(String format) {
        this.format = format;
    }

    public abstract String getRequestInfoId();

    public abstract String getSDKVersion();

    public abstract JSONObject toRequestJSONObject();
}
