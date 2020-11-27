/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.interstitial.business;

import android.content.Context;

import com.anythink.core.common.FormatLoadParams;
import com.anythink.interstitial.api.ATInterstitialListener;

public class InterstitialLoadParams extends FormatLoadParams {
    ATInterstitialListener listener;
    Context context;
}
