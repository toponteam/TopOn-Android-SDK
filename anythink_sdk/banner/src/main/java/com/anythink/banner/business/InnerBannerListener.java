/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.banner.business;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.api.AdError;

public interface InnerBannerListener {
    public void onBannerLoaded(boolean isRefresh);

    public void onBannerFailed(boolean isRefresh, AdError adError);

    public void onBannerClicked(boolean isRefresh, CustomBannerAdapter customBannerAdapter);

    public void onBannerShow(boolean isRefresh);

    public void onBannerClose(boolean isRefresh, CustomBannerAdapter customBannerAdapter);

}
