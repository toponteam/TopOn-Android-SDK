/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.utils;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.china.api.ChinaDeviceDataInfo;
import com.anythink.china.common.PermissionRequestManager;
import com.anythink.china.oaid.OaidCallback;
import com.anythink.china.oaid.OaidObtainUtil;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.SPUtil;

import java.util.regex.Pattern;

public class ChinaDeviceUtils {

    private static String mac = "";
    private static String imei = "";
    private static String oaid = "";


    public static void initDeviceInfo(final Context context) {
        String spuOaid = SPUtil.getString(context, Const.SPU_NAME, "oaid", "");
        oaid = spuOaid;
        if (TextUtils.isEmpty(oaid)) {
            if (!SDKContext.getInstance().containDeniedDeviceKey(ChinaDeviceDataInfo.OAID)) {
                if (TextUtils.isEmpty(oaid)) {
                    OaidObtainUtil.initOaidInfo(context, new OaidCallback() {
                        @Override
                        public void onSuccuss(String oaid, boolean isOaidTrackLimited) {
                            //check oaid status
                            if (isInvalidOaid(oaid)) {
                                return;
                            }
                            ChinaDeviceUtils.oaid = oaid;
                            SPUtil.putString(context, Const.SPU_NAME, "oaid", oaid);
                        }

                        @Override
                        public void onFail(String errMsg) {

                        }
                    });
                }
            }
        }

        mac = MacUtils.getMac(context);
        imei = ImeiUtils.getIMEI(context);

    }

    private static boolean isInvalidOaid(String oaid) {
        return Pattern.matches("^[0-]+$", oaid);
    }

    public static String getMac() {
        if (SDKContext.getInstance().containDeniedDeviceKey(ChinaDeviceDataInfo.MAC)) {
            return "";
        }
        return mac;
    }

    public static String getImei(Context context) {
        if (SDKContext.getInstance().containDeniedDeviceKey(ChinaDeviceDataInfo.IMEI)) {
            return "";
        }
        if (TextUtils.isEmpty(imei) && PermissionRequestManager.checkPermissionGrant(context, PermissionRequestManager.READ_PHONE_STATE_PERMISSION)) {
            imei = ImeiUtils.getIMEI(context);
        }
        return imei;
    }

    public static String getOaid() {
        if (SDKContext.getInstance().containDeniedDeviceKey(ChinaDeviceDataInfo.OAID)) {
            return "";
        }
        return oaid;
    }
}
