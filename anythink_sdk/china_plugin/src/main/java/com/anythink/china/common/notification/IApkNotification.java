package com.anythink.china.common.notification;

import android.app.NotificationManager;
import android.content.Context;

import com.anythink.china.common.download.ApkRequest;

public interface IApkNotification {

    NotificationManager getNotificationManager(Context context);

    void showNotification(ApkRequest apkRequest, long progress, long all, boolean forceShow);

    String getChannelId(ApkRequest apkRequest);
}
