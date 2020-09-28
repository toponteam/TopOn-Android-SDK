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
