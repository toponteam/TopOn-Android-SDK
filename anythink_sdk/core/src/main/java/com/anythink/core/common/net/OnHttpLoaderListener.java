/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.net;

import com.anythink.core.api.AdError;

public interface OnHttpLoaderListener {

    void onLoadStart(int reqCode);

    void onLoadFinish(int reqCode, Object result);

    void onLoadError(int reqCode, String msg, AdError errorCode);

    void onLoadCanceled(int reqCode);
}
