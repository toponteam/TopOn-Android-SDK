/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.interstitial.business;

import android.content.Context;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.AdError;
import com.anythink.core.common.CommonMediationManager;
import com.anythink.interstitial.api.ATInterstitialListener;

/**
 * MediationGroupManager
 */
public class MediationGroupManager extends CommonMediationManager {
    ATInterstitialListener mCallbackListener;

    protected MediationGroupManager(Context context) {
        super(context);
    }


    @Override
    public void prepareFormatAdapter(ATBaseAdAdapter baseAdapter) {

    }

    public void setCallbackListener(ATInterstitialListener listener) {
        mCallbackListener = listener;
    }

    @Override
    public void onDevelopLoaded() {
        if (mIsRefresh) {
            return;
        }
        if (mCallbackListener != null) {
            mCallbackListener.onInterstitialAdLoaded();
        }
        mCallbackListener = null;
    }

    @Override
    public void onDeveloLoadFail(AdError adError) {
        if (mIsRefresh) {
            return;
        }
        if (mCallbackListener != null) {
            mCallbackListener.onInterstitialAdLoadFail(adError);
        }
        mCallbackListener = null;
    }

    @Override
    public void removeFormatCallback() {
        mCallbackListener = null;
    }
}
