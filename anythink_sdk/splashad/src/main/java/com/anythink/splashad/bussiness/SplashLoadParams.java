/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.bussiness;

import android.app.Activity;
import android.view.ViewGroup;

import com.anythink.core.common.FormatLoadParams;
import com.anythink.splashad.api.ATSplashAdListener;

public class SplashLoadParams extends FormatLoadParams {
    Activity activity;
    ViewGroup containerView;
    ATSplashAdListener listener;
}
