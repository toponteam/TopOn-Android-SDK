/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anythink.china.common.notification.ApkNotificationManager;
import com.anythink.core.common.utils.CommonLogUtil;

public class NotificationBroadcaseReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationBroadcaseReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String url = intent.getStringExtra(ApkNotificationManager.BROADCAST_RECEIVER_EXTRA);

        switch (action) {
            case ApkNotificationManager.ACTION_NOTIFICATION_CLICK:
                CommonLogUtil.i(TAG, "onReceive: click...");
                ApkDownloadManager.getInstance(context).onClickNotification(url);

                break;
            case ApkNotificationManager.ACTION_NOTIFICATION_CANNEL:
                CommonLogUtil.i(TAG, "onReceive: cancel...");
                ApkDownloadManager.getInstance(context).onCleanNotification(url);
                break;
        }
    }
}
