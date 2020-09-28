package com.anythink.banner.unitgroup.api;

import android.view.View;

import com.anythink.banner.api.ATBannerView;
import com.anythink.core.api.ATBaseAdAdapter;

public abstract class CustomBannerAdapter extends ATBaseAdAdapter {

    protected CustomBannerEventListener mImpressionEventListener;
    protected ATBannerView mATBannerView;

    @Override
    final public boolean isAdReady() {
        return getBannerView() != null;
    }

    public abstract View getBannerView();

    public void setAdEventListener(CustomBannerEventListener impressionEventListener) {
        mImpressionEventListener = impressionEventListener;
    }

    final public void setATBannerView(ATBannerView atBannerView) {
        this.mATBannerView = atBannerView;
    }

    final public void releaseLoadResource() {
        super.releaseLoadResource();
        this.mATBannerView = null;
    }
}
