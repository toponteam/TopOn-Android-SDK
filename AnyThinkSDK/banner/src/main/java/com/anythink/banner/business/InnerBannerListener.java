package com.anythink.banner.business;

import com.anythink.core.api.AdError;

public interface InnerBannerListener {
    public void onBannerLoaded(boolean isRefresh);

    public void onBannerFailed(boolean isRefresh, AdError adError);

    public void onBannerClicked(boolean isRefresh);

    public void onBannerShow(boolean isRefresh);

    public void onBannerClose(boolean isRefresh);

}
