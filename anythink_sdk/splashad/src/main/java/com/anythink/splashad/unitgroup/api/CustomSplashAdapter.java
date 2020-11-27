/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.unitgroup.api;

import android.view.ViewGroup;

import com.anythink.core.api.ATBaseAdAdapter;


public abstract class CustomSplashAdapter extends ATBaseAdAdapter {
    protected ViewGroup mContainer;
    protected CustomSplashEventListener mImpressionListener;

    final public void initSplashImpressionListener(CustomSplashEventListener customSplashEventListener) {
        mImpressionListener = customSplashEventListener;
    }

    final public void initAdContainer(ViewGroup viewGroup) {
        mContainer = viewGroup;
    }

    @Override
    final public boolean isAdReady() {
        return false;
    }

//    @Override
//    final public void releaseLoadResource() {
//        super.releaseLoadResource();
//    }

    final public void cleanImpressionListener() {
        mImpressionListener = null;
    }
}
