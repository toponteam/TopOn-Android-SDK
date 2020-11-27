/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.common.notification;

import android.app.NotificationManager;
import android.content.Context;

import com.anythink.china.common.download.ApkRequest;

public interface IApkNotification {

    NotificationManager getNotificationManager(Context context);

    void showNotification(ApkRequest apkRequest, long progress, long all, boolean forceShow);

    String getChannelId(ApkRequest apkRequest);
}
