/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.bussiness;

import android.content.Context;

import com.anythink.core.common.FormatLoadParams;

public class SplashLoadParams extends FormatLoadParams {
    Context context;
    AdLoadListener listener;
    int timeout;
}
