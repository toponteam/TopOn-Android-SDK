/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

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
