/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.pd;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.china.activity.ApkConfirmDialogActivity;
import com.anythink.china.common.ApkDownloadManager;
import com.anythink.china.utils.ChinaDeviceUtils;
import com.anythink.core.api.IExHandler;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.strategy.AppStrategy;

import org.json.JSONObject;

public class ExHandler implements IExHandler {
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

    @Override
    public void handleOfferClick(final Context context, final BaseAdRequestInfo baseAdRequestInfo, final BaseAdContent baseAdContent, final String url, final String clickId, final Runnable runnable) {
        if (1 == baseAdRequestInfo.baseAdSetting.getApkDownloadConfirm()) {
            ApkConfirmDialogActivity.start(context, baseAdContent.getTitle(), new Runnable() {
                @Override
                public void run() {
                    ApkDownloadManager.getInstance(context).realStartDownloadApp(context, baseAdRequestInfo, baseAdContent, url, clickId, runnable);
                }
            });
        } else {
            ApkDownloadManager.getInstance(context).realStartDownloadApp(context, baseAdRequestInfo, baseAdContent, url, clickId, runnable);
        }
    }

}
