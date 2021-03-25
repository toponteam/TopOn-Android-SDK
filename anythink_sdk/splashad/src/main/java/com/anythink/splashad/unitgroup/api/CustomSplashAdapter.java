/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.unitgroup.api;

import android.app.Activity;
import android.view.ViewGroup;

import com.anythink.core.api.ATBaseAdAdapter;


public abstract class CustomSplashAdapter extends ATBaseAdAdapter {
    protected CustomSplashEventListener mImpressionListener;
    protected int mFetchAdTimeout;

    final public void internalShow(Activity activity, ViewGroup container, CustomSplashEventListener listener) {
        mImpressionListener = listener;
        show(activity, container);
    }

    final public void setFetchAdTimeout(int timeout) {
        mFetchAdTimeout = timeout;
    }

    public abstract void show(Activity activity, ViewGroup container);

    final public void cleanImpressionListener() {
        mImpressionListener = null;
    }
}
