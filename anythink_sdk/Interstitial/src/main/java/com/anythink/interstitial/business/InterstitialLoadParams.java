package com.anythink.interstitial.business;

import android.content.Context;

import com.anythink.core.common.FormatLoadParams;
import com.anythink.interstitial.api.ATInterstitialListener;

public class InterstitialLoadParams extends FormatLoadParams {
    ATInterstitialListener listener;
    Context context;
}
