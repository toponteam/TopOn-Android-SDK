/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.api;

import com.anythink.core.api.AdError;
import com.anythink.core.api.ATAdInfo;

public interface ATSplashAdListener {
    public void onAdLoaded();

    public void onNoAdError(AdError adError);

    public void onAdShow(ATAdInfo entity);

    public void onAdClick(ATAdInfo entity);

    public void onAdDismiss(ATAdInfo entity);

    @Deprecated
    public void onAdTick(long millisUtilFinished);
}
