/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.adx.manager;

import android.content.Context;
import android.content.IntentFilter;

import com.anythink.basead.adx.AdxBroadcastReceiver;
import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.china.common.ApkDownloadManager;
import com.anythink.core.common.entity.AdxOffer;

import java.util.concurrent.ConcurrentHashMap;

public class AdxApkManager {

    private static AdxApkManager sIntance;
    private Context mContext;

    private ConcurrentHashMap<String, AdxOffer> mMap;

    AdxBroadcastReceiver mAdxBroadcastReceiver;


    private AdxApkManager(Context context) {
        mContext = context;
        mMap = new ConcurrentHashMap<>();
    }

    public synchronized static AdxApkManager getInstance(Context context) {
        if (sIntance == null) {
            sIntance = new AdxApkManager(context);
        }
        return sIntance;
    }

    public void registerAdxApkBroadcastReceiver() {
        if (mContext == null) {
            return;
        }
        if (mAdxBroadcastReceiver == null) {
            mAdxBroadcastReceiver = new AdxBroadcastReceiver();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ApkDownloadManager.ACTION_APK_DOWNLOAD_START);
            intentFilter.addAction(ApkDownloadManager.ACTION_APK_DOWNLOAD_END);
            intentFilter.addAction(ApkDownloadManager.ACTION_APK_INSTALL_SUCCESSFUL);

            mContext.registerReceiver(mAdxBroadcastReceiver, intentFilter);
        }
    }

    public void unregisterAdxApkBroadcastReceiver() {
        if (mAdxBroadcastReceiver != null) {
            mContext.unregisterReceiver(mAdxBroadcastReceiver);
            mAdxBroadcastReceiver = null;
        }
    }

    public void register(String offerId, AdxOffer adxOffer) {
        mMap.put(offerId, adxOffer);
    }

    public void onApkDownloadStart(String offerId) {
        AdxOffer adxOffer = mMap.get(offerId);
        if (adxOffer != null) {
            OfferAdFunctionUtil.sendAdxAdTracking(OfferAdFunctionUtil.APK_DOWNLOAD_START_TYPE, adxOffer);
        }
    }

    public void onApkDownloadEnd(String offerId) {
        AdxOffer adxOffer = mMap.get(offerId);
        if (adxOffer != null) {
            OfferAdFunctionUtil.sendAdxAdTracking(OfferAdFunctionUtil.APK_DOWNLOAD_END_TYPE, adxOffer);
        }
    }

    public void onApkInstallSuccessful(String offerId) {
        AdxOffer adxOffer = mMap.remove(offerId);
        if (adxOffer != null) {
            OfferAdFunctionUtil.sendAdxAdTracking(OfferAdFunctionUtil.APK_INSTALL_TYPE, adxOffer);
        }

        if (mMap.size() == 0) {
            this.unregisterAdxApkBroadcastReceiver();
        }
    }
}
