/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.api;

import android.content.Context;

import com.anythink.china.common.PermissionRequestManager;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IIdentifierListener;

public class ATChinaSDKHandler {
    /**
     * Use by developer
     *
     * @param context
     */
    public static void requestPermissionIfNecessary(Context context) {
        PermissionRequestManager.requestPermission(context, null, PermissionRequestManager.READ_PHONE_STATE_PERMISSION, PermissionRequestManager.WRITE_EXTERNAL_STORAGE_PERMISSION);
    }

    public static void handleInitOaidSDK(Context context, final OaidSDKCallbackListener oaidSDKListener) {
        try {
            MdidSdkHelper.InitSdk(context.getApplicationContext(), true, new IIdentifierListener() {
                @Override
                public void OnSupport(boolean b, com.bun.miitmdid.interfaces.IdSupplier idSupplier) {
                    if (oaidSDKListener != null) {
                        oaidSDKListener.OnSupport(b, idSupplier);
                    }
                }
            });
        } catch (Throwable e) {

        }

    }
}
