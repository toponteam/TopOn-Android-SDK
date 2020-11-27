/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.interstitial.unitgroup.api;

import android.app.Activity;
import android.content.Context;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.ATMediationSetting;
import com.anythink.core.common.base.AnyThinkBaseAdapter;

import java.util.Map;

public abstract class CustomInterstitialAdapter extends ATBaseAdAdapter {

    protected CustomInterstitialEventListener mImpressListener;

    public abstract void show(Activity activity);

    final public void internalShow(Activity activity, CustomInterstitialEventListener listener) {
        mImpressListener = listener;
        show(activity);
    }

    public void clearImpressionListener() {
        mImpressListener = null;
    }

}
