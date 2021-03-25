/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.splashad.api;

import com.anythink.core.api.ATAdInfo;

public interface ATSplashExListener extends ATSplashAdListener {
    void onDeeplinkCallback(ATAdInfo entity, boolean isSuccess);
}
