/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness;

import android.content.Context;
import android.content.IntentFilter;

import com.anythink.basead.entity.ConversionRecord;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.china.common.ApkDownloadManager;
import com.anythink.core.common.entity.BaseAdContent;

import java.util.concurrent.ConcurrentHashMap;

public class OfferStatusManager {

    private static OfferStatusManager sIntance;
    private Context mContext;

    private ConcurrentHashMap<String, BaseAdContent> mMap;

    OfferStatusBroadcastReceiver mOfferStatusBroadcastReceiver;


    private OfferStatusManager(Context context) {
        mContext = context;
        mMap = new ConcurrentHashMap<>();
    }

    public synchronized static OfferStatusManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new OfferStatusManager(context);
        }
        return sIntance;
    }

    public void registerOfferStatusBroadcastReceiver() {
        if (mContext == null) {
            return;
        }
        if (mOfferStatusBroadcastReceiver == null) {
            mOfferStatusBroadcastReceiver = new OfferStatusBroadcastReceiver();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ApkDownloadManager.ACTION_OFFER_DOWNLOAD_START);
            intentFilter.addAction(ApkDownloadManager.ACTION_OFFER_DOWNLOAD_END);
            intentFilter.addAction(ApkDownloadManager.ACTION_OFFER_INSTALL_START);
            intentFilter.addAction(ApkDownloadManager.ACTION_OFFER_INSTALL_SUCCESSFUL);

            mContext.registerReceiver(mOfferStatusBroadcastReceiver, intentFilter);
        }
    }

    public void unregisterApkBroadcastReceiver() {
        if (mOfferStatusBroadcastReceiver != null) {
            mContext.unregisterReceiver(mOfferStatusBroadcastReceiver);
            mOfferStatusBroadcastReceiver = null;
        }
    }

    public void register(String offerId, BaseAdContent baseAdContent) {
        mMap.put(offerId, baseAdContent);
    }

    public void onOfferDownloadStart(String offerId, String clickId) {
        BaseAdContent baseAdContent = mMap.get(offerId);
        if (baseAdContent != null) {
            UserOperateRecord userOperateRecord =new UserOperateRecord("","");
            userOperateRecord.conversionRecord = new ConversionRecord();
            userOperateRecord.conversionRecord.clickId = clickId;
            OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APK_DOWNLOAD_START_TYPE, baseAdContent, userOperateRecord);
        }
    }

    public void onOfferDownloadEnd(String offerId, String clickId) {
        BaseAdContent baseAdContent = mMap.get(offerId);
        if (baseAdContent != null) {
            UserOperateRecord userOperateRecord =new UserOperateRecord("","");
            userOperateRecord.conversionRecord = new ConversionRecord();
            userOperateRecord.conversionRecord.clickId = clickId;
            OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APK_DOWNLOAD_END_TYPE, baseAdContent, userOperateRecord);
        }
    }

    public void onOfferInstallStart(String offerId, String clickId) {
        BaseAdContent baseAdContent = mMap.get(offerId);
        if (baseAdContent != null) {
            UserOperateRecord userOperateRecord =new UserOperateRecord("","");
            userOperateRecord.conversionRecord = new ConversionRecord();
            userOperateRecord.conversionRecord.clickId = clickId;
            OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APK_INSTALL_START_TYPE, baseAdContent, userOperateRecord);
        }
    }

    public void onOfferInstallSuccessful(String offerId, String clickId) {
        BaseAdContent baseAdContent = mMap.remove(offerId);
        if (baseAdContent != null) {
            UserOperateRecord userOperateRecord =new UserOperateRecord("","");
            userOperateRecord.conversionRecord = new ConversionRecord();
            userOperateRecord.conversionRecord.clickId = clickId;
            OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.APK_INSTALL_FINISH_TYPE, baseAdContent, userOperateRecord);
        }

        if (mMap.size() == 0) {
            this.unregisterApkBroadcastReceiver();
        }
    }
}
