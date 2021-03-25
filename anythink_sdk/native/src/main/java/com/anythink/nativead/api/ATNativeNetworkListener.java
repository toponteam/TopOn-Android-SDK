/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.api;

import com.anythink.core.api.AdError;


public interface ATNativeNetworkListener {
    /**
     * Ad Request Success Callback
     */
    void onNativeAdLoaded();

    /**
     * Ad Request Fail Callback
     */
    void onNativeAdLoadFail(AdError error);

}
