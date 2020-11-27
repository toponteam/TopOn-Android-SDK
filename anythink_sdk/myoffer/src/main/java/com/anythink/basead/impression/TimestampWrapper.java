/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.impression;

import android.os.SystemClock;

class TimestampWrapper<T> {
    final T mInstance;
    long mCreatedTimestamp;

    TimestampWrapper(final T instance) {
        mInstance = instance;
        mCreatedTimestamp = SystemClock.uptimeMillis();
    }
}
