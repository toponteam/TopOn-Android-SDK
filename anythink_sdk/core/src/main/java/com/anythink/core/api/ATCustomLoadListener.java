/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

public interface ATCustomLoadListener {
    public void onAdDataLoaded();

    public void onAdCacheLoaded(BaseAd... baseAds);

    public void onAdLoadError(String errorCode, String errorMsg);
}
