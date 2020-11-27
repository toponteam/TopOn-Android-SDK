/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.adx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anythink.basead.adx.manager.AdxApkManager;
import com.anythink.china.common.ApkDownloadManager;

public class AdxBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action != null) {
            String offerId = intent.getStringExtra(ApkDownloadManager.RECEIVER_EXTRA_OFFER_ID);
            switch (action) {
                case ApkDownloadManager.ACTION_APK_DOWNLOAD_START:
                    AdxApkManager.getInstance(context.getApplicationContext()).onApkDownloadStart(offerId);
                    break;
                case ApkDownloadManager.ACTION_APK_DOWNLOAD_END:
                    AdxApkManager.getInstance(context.getApplicationContext()).onApkDownloadEnd(offerId);
                    break;
                case ApkDownloadManager.ACTION_APK_INSTALL_SUCCESSFUL:
                    AdxApkManager.getInstance(context.getApplicationContext()).onApkInstallSuccessful(offerId);
                    break;
                default:
                    break;
            }
        }
    }
}
