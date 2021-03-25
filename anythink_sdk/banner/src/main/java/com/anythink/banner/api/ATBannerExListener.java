/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.banner.api;


import com.anythink.core.api.ATAdInfo;

/**
 * Banner Event Listener
 */
public interface ATBannerExListener extends ATBannerListener {
    /**
     * Deeplink result callback
     *
     * @param isSuccess
     */
    void onDeeplinkCallback(boolean isRefresh, ATAdInfo adInfo, boolean isSuccess);
}
