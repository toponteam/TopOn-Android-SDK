package com.anythink.banner.api;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;

/**
 * Banner Event Listener
 */
public interface ATBannerListener {
    public void onBannerLoaded();

    public void onBannerFailed(AdError adError);

    public void onBannerClicked(ATAdInfo adInfo);

    public void onBannerShow(ATAdInfo adInfo);

    public void onBannerClose(ATAdInfo adInfo);

    public void onBannerAutoRefreshed(ATAdInfo adInfo);

    public void onBannerAutoRefreshFail(AdError adError);

}
