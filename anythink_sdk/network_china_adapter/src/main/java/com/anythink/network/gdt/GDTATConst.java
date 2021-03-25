/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.gdt;

import com.qq.e.comm.managers.status.SDKStatus;


public class GDTATConst {
    public static final String AD_HEIGHT = "gdtad_height";

    public static final int NETWORK_FIRM_ID = 8;

    public static String getNetworkVersion() {
        try {
            return SDKStatus.getSDKVersion();
        } catch (Throwable e) {

        }
        return "";
    }
}
