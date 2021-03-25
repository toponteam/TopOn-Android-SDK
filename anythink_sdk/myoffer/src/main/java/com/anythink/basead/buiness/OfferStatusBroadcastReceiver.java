/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anythink.china.common.ApkDownloadManager;

public class OfferStatusBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action != null) {
            String offerId = intent.getStringExtra(ApkDownloadManager.RECEIVER_EXTRA_OFFER_ID);
            String clickId = intent.getStringExtra(ApkDownloadManager.RECEIVER_EXTRA_CLICK_ID);
            switch (action) {
                case ApkDownloadManager.ACTION_OFFER_DOWNLOAD_START:
                    OfferStatusManager.getInstance(context.getApplicationContext()).onOfferDownloadStart(offerId, clickId);
                    break;
                case ApkDownloadManager.ACTION_OFFER_DOWNLOAD_END:
                    OfferStatusManager.getInstance(context.getApplicationContext()).onOfferDownloadEnd(offerId, clickId);
                    break;
                case ApkDownloadManager.ACTION_OFFER_INSTALL_SUCCESSFUL:
                    OfferStatusManager.getInstance(context.getApplicationContext()).onOfferInstallSuccessful(offerId, clickId);
                    break;
                case ApkDownloadManager.ACTION_OFFER_INSTALL_START:
                    OfferStatusManager.getInstance(context.getApplicationContext()).onOfferInstallStart(offerId, clickId);
                    break;
                default:
                    break;
            }
        }
    }
}
