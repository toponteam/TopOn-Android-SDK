/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 *
 */

package com.anythink.network.mytarget;

import com.my.target.common.MyTargetVersion;

import java.lang.reflect.Field;

public class MyTargetATConst {

    public static final int NETWORK_FIRM_ID = 32;

    public static String getNetworkVersion() {
        try {

            Class<?> aClass = Class.forName("com.my.target.common.MyTargetVersion");
            Field version = aClass.getDeclaredField("VERSION");
            version.setAccessible(true);

            return version.get(aClass).toString();
        } catch (Throwable e) {
            return MyTargetVersion.VERSION;
        }
    }
}
