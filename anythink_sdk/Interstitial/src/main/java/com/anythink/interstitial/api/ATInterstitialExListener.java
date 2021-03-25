/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.interstitial.api;

import com.anythink.core.api.ATAdInfo;


public interface ATInterstitialExListener extends ATInterstitialListener {

    void onDeeplinkCallback(ATAdInfo adInfo, boolean isSuccess);

}
