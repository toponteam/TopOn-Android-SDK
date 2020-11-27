/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

import android.content.Context;


import java.util.List;
import java.util.Map;

public abstract class ATInitMediation {


    public abstract void initSDK(Context context, Map<String, Object> serviceExtras);

    public boolean setUserDataConsent(Context context, boolean isConsent, boolean isEUTraffic) {
        return false;
    }

    public String getNetworkName() {
        return "";
    }

    public String getNetworkSDKClass() {
        return "";
    }

    public Map<String, Boolean> getPluginClassStatus() {
        return null;
    }

    public List getActivityStatus() {
        return null;
    }

    public List getServiceStatus() {
        return null;
    }

    public List getProviderStatus() {
        return null;
    }
}
