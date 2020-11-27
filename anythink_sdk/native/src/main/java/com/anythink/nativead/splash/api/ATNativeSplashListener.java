/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.splash.api;

import com.anythink.core.api.ATAdInfo;

public interface ATNativeSplashListener {
    public void onAdLoaded();

    public void onNoAdError(String msg);

    public void onAdShow(ATAdInfo entity);

    public void onAdClick(ATAdInfo entity);

    public void onAdSkip();

    public void onAdTimeOver();

    public void onAdTick(long millisUtilFinished);
}
