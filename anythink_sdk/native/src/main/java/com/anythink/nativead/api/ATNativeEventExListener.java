/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.api;

import com.anythink.core.api.ATAdInfo;

public interface ATNativeEventExListener extends ATNativeEventListener {
    /**
     * Deeplink result callback
     *
     * @param view
     * @param isSuccess
     */
    public void onDeeplinkCallback(ATNativeAdView view, ATAdInfo adInfo, boolean isSuccess);
}
