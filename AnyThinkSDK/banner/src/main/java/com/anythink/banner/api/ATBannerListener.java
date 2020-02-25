package com.anythink.banner.api;

import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.AdError;

/**
 * Banner Event Listener
 */
public interface ATBannerListener {
    public void onBannerLoaded();

    public void onBannerFailed(AdError adError);

    public void onBannerClicked(ATAdInfo entity);

    public void onBannerShow(ATAdInfo entity);

    public void onBannerClose();

    public void onBannerAutoRefreshed(ATAdInfo entity);

    public void onBannerAutoRefreshFail(AdError adError);

}
