/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.api;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.china.common.PermissionRequestManager;
import com.anythink.china.utils.ChinaDeviceUtils;
import com.anythink.core.api.IATChinaSDKHandler;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.strategy.AppStrategy;

import org.json.JSONObject;

public class ATChinaSDKHandler implements IATChinaSDKHandler {

    public static final String JSON_REQUEST_MAC = "mac";
    public static final String JSON_REQUEST_IMEI = "imei";
    public static final String JSON_REQUEST_OAID = "oaid";

    @Override
    public void initDeviceInfo(Context context) {
        ChinaDeviceUtils.initDeviceInfo(context);
    }

    @Override
    public void fillRequestData(JSONObject jsonObject, AppStrategy appStrategy) {
        String dataLevel = appStrategy != null ? appStrategy.getDataLevel() : "";
        if (TextUtils.isEmpty(dataLevel)) {
            try {
                jsonObject.put(JSON_REQUEST_MAC, ChinaDeviceUtils.getMac());
                jsonObject.put(JSON_REQUEST_IMEI, ChinaDeviceUtils.getImei(SDKContext.getInstance().getContext()));
                jsonObject.put(JSON_REQUEST_OAID, ChinaDeviceUtils.getOaid());
            } catch (Exception e) {

            }
        } else {
            int macOpen = 1;
            int imeiOpen = 1;
            try {
                JSONObject leveObject = new JSONObject(dataLevel);
                macOpen = leveObject.optInt("m");
                imeiOpen = leveObject.optInt("i");
            } catch (Exception e) {

            }

            try {
                jsonObject.put(JSON_REQUEST_MAC, (macOpen == 1 ? ChinaDeviceUtils.getMac() : ""));
                jsonObject.put(JSON_REQUEST_IMEI, (imeiOpen == 1 ? ChinaDeviceUtils.getImei(SDKContext.getInstance().getContext()) : ""));
                jsonObject.put(JSON_REQUEST_OAID, ChinaDeviceUtils.getOaid());
            } catch (Exception e) {

            }
        }
    }

    /**
     * Use by developer
     *
     * @param context
     */
    public void requestPermissionIfNecessary(Context context) {
        PermissionRequestManager.requestPermission(context, null, PermissionRequestManager.READ_PHONE_STATE_PERMISSION, PermissionRequestManager.WRITE_EXTERNAL_STORAGE_PERMISSION);
    }
}
