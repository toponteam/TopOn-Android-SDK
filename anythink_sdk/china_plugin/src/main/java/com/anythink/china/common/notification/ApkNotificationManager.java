package com.anythink.china.common.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.anythink.china.common.NotificationBroadcaseReceiver;
import com.anythink.china.common.download.ApkRequest;
import com.anythink.core.common.utils.CommonUtil;

import java.util.HashMap;
import java.util.Map;

public class ApkNotificationManager implements IApkNotification {

    public static final String TAG = ApkNotificationManager.class.getSimpleName();

    public static final String ACTION_NOTIFICATION_CLICK = "action_notification_click";
    public static final String ACTION_NOTIFICATION_CANNEL = "action_notification_cannel";
    public static final String BROADCAST_RECEIVER_EXTRA = "broadcast_receiver_extra";

    private NotificationManager mNotificationManager;

    //ChannelId -> NotificationEntity
    Map<String, NotificationEntity> mNotificationEntityMap;


    private static ApkNotificationManager sInstance;
    private Context mContext;
    private int id;

    public static synchronized ApkNotificationManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ApkNotificationManager(context);
        }
        return sInstance;
    }

    private ApkNotificationManager(Context context) {
        mContext = context;

        mNotificationManager = getNotificationManager(context);
        mNotificationEntityMap = new HashMap<>();
    }


    @Override
    public NotificationManager getNotificationManager(Context context) {
        if (context == null) {
            return null;
        }
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void showNotification(ApkRequest apkRequest, long progress, long all, boolean forceShow) {
        if (apkRequest == null || TextUtils.isEmpty(apkRequest.url)) {
            return;
        }
        try {
            if (mNotificationManager == null) {
                mNotificationManager = getNotificationManager(mContext);
            }

            NotificationEntity notificationEntity = getNotificationEntity(apkRequest);
            NotificationCompat.Builder builder = notificationEntity.builder;
            int lastPercent = notificationEntity.percent;


            boolean canCancel = false;
            int currentPercent;
            String text = "default";
            if (progress >= all) {
                currentPercent = 100;
                text = "点击安装";
                canCancel = true;
            } else {
                currentPercent = getPercent(progress, all);

                if (!forceShow && lastPercent == currentPercent) {//if percent do not change, do nothing
                    return;
                }
                //            Log.i("apk", "showNotification: percent changed to " + currentPercent);
                notificationEntity.percent = currentPercent;

                if (apkRequest.isIdle()) {
                    text = "等待下载中";
                    canCancel = false;
                } else if (apkRequest.isPause()) {
                    text = "点击继续下载（已完成：" + currentPercent + "%)";
                    canCancel = true;
                } else if (apkRequest.isLoading()) {
                    text = "点击暂停下载（下载中：" + currentPercent + "%)";
                    canCancel = false;
                }
            }
            builder.setProgress(100, currentPercent, false)
                    .setContentText(text)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text));

            if (canCancel) {
                builder.setOngoing(false)
                        .setAutoCancel(true);
            } else {
                builder.setOngoing(true)
                        .setAutoCancel(false);
            }

//            Log.i(TAG, "showNotification: " + apkRequest.title + ", " + text);
            mNotificationManager.notify(notificationEntity.id, builder.build());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void showNotification(ApkRequest apkRequest, long progress, long all) {
        showNotification(apkRequest, progress, all, false);
    }

    public void showClickInstallNotification(ApkRequest apkRequest) {
        showNotification(apkRequest, 100, 100, true);
    }

    public void showWaitingNotification(ApkRequest apkRequest) {
        showNotification(apkRequest, 0, 100, true);
    }

    private int getPercent(long progress, long all) {
        return (int) ((progress * 1.0f / all) * 100);
    }


    private NotificationEntity getNotificationEntity(ApkRequest apkRequest) {
        String channelId = getChannelId(apkRequest);
        NotificationEntity cacheNotificationEntity = mNotificationEntityMap.get(channelId);
        if (cacheNotificationEntity != null) {
            return cacheNotificationEntity;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            NotificationChannel notificationChannel = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                notificationChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);

                notificationChannel.setSound(null, null);
                mNotificationManager.createNotificationChannel(notificationChannel);
            }
        }

        this.id++;

        //click and cancel
        Intent clickIntent = new Intent(ACTION_NOTIFICATION_CLICK);
        clickIntent.putExtra(BROADCAST_RECEIVER_EXTRA, apkRequest.url);
        clickIntent.setClass(mContext, NotificationBroadcaseReceiver.class);
        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(mContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent cancelIntent = new Intent(ACTION_NOTIFICATION_CANNEL);
        cancelIntent.putExtra(BROADCAST_RECEIVER_EXTRA, apkRequest.url);
        cancelIntent.setClass(mContext, NotificationBroadcaseReceiver.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(mContext, id, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(clickPendingIntent)
                .setDeleteIntent(cancelPendingIntent);


        //can not cancel by swipe
        builder.setOngoing(true)
                .setSound(null)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false);


        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            builder.setSmallIcon(applicationInfo.icon);
        } catch (Throwable e) {
            e.printStackTrace();
            builder.setSmallIcon(CommonUtil.getResId(mContext, "core_icon_close", "drawable"));
        }


        builder.setContentTitle(apkRequest.title)
                .setLargeIcon(apkRequest.icon);

        cacheNotificationEntity = new NotificationEntity();

        cacheNotificationEntity.id = this.id;
        cacheNotificationEntity.builder = builder;
        cacheNotificationEntity.percent = -1;

        mNotificationEntityMap.put(channelId, cacheNotificationEntity);

        return cacheNotificationEntity;
    }

    @Override
    public String getChannelId(ApkRequest apkRequest) {
        return apkRequest.url;
    }

    public void cancelNotification(ApkRequest apkRequest) {
        if (apkRequest == null || TextUtils.isEmpty(apkRequest.url) || mNotificationManager == null) {
            return;
        }
        NotificationEntity notificationEntity = getNotificationEntity(apkRequest);
        mNotificationManager.cancel(notificationEntity.id);
        mNotificationEntityMap.remove(getChannelId(apkRequest));
    }

    public void cancelAllNotification() {
//        Log.i(TAG, "cancelAllNotification: ");
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }
}
