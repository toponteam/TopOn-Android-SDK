/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.banner.api;

import com.anythink.core.api.ATAdInfo;

public interface ATNativeBannerListener {

    public void onAdLoaded();

    public void onAdError(String errorMsg);

    public void onAdClick(ATAdInfo entity);

    public void onAdClose();

    public void onAdShow(ATAdInfo entity);

    public void onAutoRefresh(ATAdInfo entity);

    public void onAutoRefreshFail(String errorMsg);
}
