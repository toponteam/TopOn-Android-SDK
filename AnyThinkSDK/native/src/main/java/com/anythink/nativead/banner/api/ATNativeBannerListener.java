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
