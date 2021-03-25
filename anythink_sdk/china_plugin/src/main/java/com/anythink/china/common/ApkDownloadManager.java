/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.common;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.anythink.china.common.download.ApkBaseLoader;
import com.anythink.china.common.download.ApkRequest;
import com.anythink.china.common.download.IApkManager;
import com.anythink.china.common.notification.ApkNotificationManager;
import com.anythink.china.common.resource.ApkResource;
import com.anythink.china.common.resource.FileUtils;
import com.anythink.china.common.service.ApkDownloadService;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.track.AgentEventManager;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonMD5;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApkDownloadManager implements IApkManager {

    public static final String TAG = ApkDownloadManager.class.getSimpleName();
    private static ApkDownloadManager sInstance;
    private Context mContext;

    public static final String ACTION_OFFER_DOWNLOAD_START = "action_offer_download_start";
    public static final String ACTION_OFFER_DOWNLOAD_END = "action_offer_download_end";
    public static final String ACTION_OFFER_INSTALL_START = "action_offer_install_start";
    public static final String ACTION_OFFER_INSTALL_SUCCESSFUL = "action_offer_install_successful";
    public static final String RECEIVER_EXTRA_OFFER_ID = "receiver_extra_offer_id";
    public static final String RECEIVER_EXTRA_CLICK_ID = "receiver_extra_click_id";

    private LinkedList<ApkRequest> mRequestQueue;
    private Map<String, ApkRequest> mDownloadingRequestMap;//apkRequest.uniqueID -> apkRequest
    private Map<String, ApkRequest> mPauseRequestMap;//apkRequest.uniqueID -> apkRequest
    private Map<String, ApkBaseLoader.DownloadListener> mApkLoaderListener;//apkRequest.uniqueID -> downloadListener
    private Map<String, ApkRequest> mSuccessRequestMap;//apkRequest.uniqueID -> apkRequest
    private Map<String, ApkRequest> mInstallingPackageMap;//pkgName -> apkRequest

    private final int mDownloadCount = 1;
    private long mOfferCacheTime = 7 * 24 * 60 * 60 * 1000L;

    private BroadcastReceiver mApkInstallBroadcastReceiver;
    private ApkDownloadService.ApkDownloadBinder mDownloadBinder;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDownloadBinder = (ApkDownloadService.ApkDownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDownloadBinder = null;
        }
    };

    private ApkDownloadManager(Context context) {
        mContext = context.getApplicationContext();

        mRequestQueue = new LinkedList<>();
        mDownloadingRequestMap = new HashMap<>();
        mPauseRequestMap = new HashMap<>();
        mApkLoaderListener = new HashMap<>();

        //ensure save diretory is exist
        String saveDirectory = FileUtils.getSaveDirectory();
        if (!TextUtils.isEmpty(saveDirectory)) {
            File file = new File(saveDirectory);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    public static synchronized ApkDownloadManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ApkDownloadManager(context);
        }
        return sInstance;
    }

    public void realStartDownloadApp(final Context context, final BaseAdRequestInfo baseAdRequestInfo
            , final BaseAdContent baseAdContent, final String url, String clickId, Runnable runnableBeforeStartDownload) {
        if (ApkResource.isApkInstalled(context, baseAdContent.getPkgName())) {
            //App was installed， open it
            ApkResource.openApp(context, baseAdContent.getPkgName());
        } else {

            if (runnableBeforeStartDownload != null) {
                runnableBeforeStartDownload.run();
            }

            //App not exist, download it
            ApkRequest apkRequest = new ApkRequest();
            apkRequest.uniqueID = CommonMD5.getMD5(baseAdContent.getOfferId() + baseAdRequestInfo.adsourceId + baseAdContent.getClickUrl());
            apkRequest.requestId = baseAdRequestInfo.requestId;
            apkRequest.offerId = baseAdContent.getOfferId();
            apkRequest.url = url;
            apkRequest.clickId = clickId;
            apkRequest.pkgName = baseAdContent.getPkgName();
            apkRequest.title = baseAdContent.getTitle();
            int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, context.getResources().getDisplayMetrics());
            apkRequest.icon = ImageLoader.getInstance(context).getBitmapFromDiskCache(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, baseAdContent.getIconUrl()), size, size);


            ApkDownloadManager.getInstance(context).setOfferCacheTime(baseAdRequestInfo.baseAdSetting.getOfferCacheTime());
            ApkDownloadManager.getInstance(context).checkAndCleanApk();
            ApkDownloadManager.getInstance(context).handleClick(apkRequest);
        }
    }

    public void setOfferCacheTime(long time) {
        if (time > 0) {
            this.mOfferCacheTime = time;
        }
    }


    @Override
    public void addQueue(ApkRequest apkRequest) {

        if (apkRequest == null) {
            return;
        }

        if (mDownloadingRequestMap.containsKey(apkRequest.uniqueID)) {
            //if apk is downloading, do nothing
            File tempFile = new File(FileUtils.getResourcePath(apkRequest.uniqueID) + ApkBaseLoader.SUFFIX_TEMP);
            File logFile = new File(FileUtils.getResourcePath(apkRequest.uniqueID) + ApkBaseLoader.SUFFIX_LOG);
            if (tempFile.exists() && logFile.exists()) {//check temp or log file whether exist
                Log.i(TAG, "(" + apkRequest.title + ") is downloading, do nothing");
                ApkDownloadManager.this.showToast("正在下载中： " + apkRequest.title);
                return;
            } else {
                mDownloadingRequestMap.remove(apkRequest.uniqueID);
            }
        }

        int size = mRequestQueue.size();
        for (int i = 0; i < size; i++) {
            if (TextUtils.equals(apkRequest.uniqueID, mRequestQueue.get(i).uniqueID)) {//apk is waiting for downloading
                Log.i(TAG, "(" + apkRequest.title + ") is waiting for downloading, do nothing");
                ApkDownloadManager.this.showToast("等待下载： " + apkRequest.title);
                return;
            }
        }
        //add to request queue
        mRequestQueue.add(apkRequest);

        //show waiting notification
        ApkNotificationManager.getInstance(mContext).showWaitingNotification(apkRequest);
    }

    @Override
    public int getDownloadCount() {
        return mDownloadCount;
    }

    @Override
    public boolean isApkExist(String uniqueID) {
        String apkPath = FileUtils.getResourcePath(uniqueID) + ApkBaseLoader.SUFFIX_APK;
        if (!TextUtils.isEmpty(apkPath)) {
            File file = new File(apkPath);
            return file.exists();
        }
        return false;
    }

    @Override
    public void download() {
        int size = mRequestQueue.size();
        if (size == 0) {
            return;
        }

        int count = getDownloadCount();
        if (count > size) {
            count = size;
        }

        int downloadingCount = mDownloadingRequestMap.size();
        if (downloadingCount >= count) {
            return;
        }

        count -= downloadingCount;

        ApkRequest apkRequest;
        for (int i = 0; i < count; i++) {
            apkRequest = mRequestQueue.removeFirst();

            downloadInternal(apkRequest);
        }
    }

    private void downloadInternal(ApkRequest apkRequest) {
        mDownloadingRequestMap.put(apkRequest.uniqueID, apkRequest);

        ApkBaseLoader.DownloadListener downloadListener = new ApkBaseLoader.DownloadListener() {
            @Override
            public void onStartBefore(final ApkRequest apkRequest, final long progress, final long all) {
                CommonLogUtil.i(TAG, "onStartBefore: " + apkRequest.url);
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progress < all) {
                            ApkDownloadManager.this.showToast("正在下载： " + apkRequest.title);
                            ApkNotificationManager.getInstance(mContext).cancelNotification(apkRequest);
                            ApkNotificationManager.getInstance(mContext).showNotification(apkRequest, progress, all);
                        }

                        Intent intent = new Intent();
                        intent.setAction(ApkDownloadManager.ACTION_OFFER_DOWNLOAD_START);
                        intent.setPackage(mContext.getPackageName());
                        intent.putExtra(ApkDownloadManager.RECEIVER_EXTRA_OFFER_ID, apkRequest.offerId);
                        intent.putExtra(ApkDownloadManager.RECEIVER_EXTRA_CLICK_ID, apkRequest.clickId);
                        mContext.sendBroadcast(intent);

                        AgentEventManager.onApkDownload(apkRequest.requestId, apkRequest.offerId, apkRequest.url, 1, null, 0, all);
                    }
                });
            }

            @Override
            public void onSuccess(final ApkRequest apkRequest, final long downloadTime) {
                Log.i(TAG, "onSuccess: " + apkRequest.title);
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mApkLoaderListener.remove(apkRequest.uniqueID);
                        mDownloadingRequestMap.remove(apkRequest.uniqueID);
                        if (mSuccessRequestMap == null) {
                            mSuccessRequestMap = new HashMap<>();
                        }
                        mSuccessRequestMap.put(apkRequest.uniqueID, apkRequest);

                        checkPermissionAndInstall(apkRequest);
                        ApkNotificationManager.getInstance(mContext).showClickInstallNotification(apkRequest);

                        Intent intent = new Intent();
                        intent.setAction(ApkDownloadManager.ACTION_OFFER_DOWNLOAD_END);
                        intent.setPackage(mContext.getPackageName());
                        intent.putExtra(ApkDownloadManager.RECEIVER_EXTRA_OFFER_ID, apkRequest.offerId);
                        intent.putExtra(ApkDownloadManager.RECEIVER_EXTRA_CLICK_ID, apkRequest.clickId);
                        mContext.sendBroadcast(intent);

                        AgentEventManager.onApkDownload(apkRequest.requestId, apkRequest.offerId, apkRequest.url, 2, null, downloadTime, apkRequest.apkSize);

                        //try to download next apk if exist
                        download();
                    }
                });
            }

            @Override
            public void onProgress(final ApkRequest apkRequest, final long progress, final long all) {
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        ApkNotificationManager.getInstance(mContext).showNotification(apkRequest, progress, all);
                    }
                });
            }

            @Override
            public void onFailed(final ApkRequest apkRequest, final String msg) {
                Log.e(TAG, "(" + apkRequest.title + ") download fail: " + msg);
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mApkLoaderListener.remove(apkRequest.uniqueID);
                        mDownloadingRequestMap.remove(apkRequest.uniqueID);
                        ApkNotificationManager.getInstance(mContext).cancelNotification(apkRequest);

                        AgentEventManager.onApkDownload(apkRequest.requestId, apkRequest.offerId, apkRequest.url, 3, msg, 0, apkRequest.apkSize);

                        //try to download next apk if exist
                        download();
                    }
                });
            }

            @Override
            public void onCancel(final ApkRequest apkRequest, final long progress, final long all, final int status) {
                CommonLogUtil.i(TAG, "onCancel: ");
                SDKContext.getInstance().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadingRequestMap.remove(apkRequest.uniqueID);

                        ApkNotificationManager.getInstance(mContext).cancelNotification(apkRequest);
                        if (status == ApkBaseLoader.PAUSE) {
                            Log.e(TAG, "(" + apkRequest.title + ") pause download");

                            ApkNotificationManager.getInstance(mContext).showNotification(apkRequest, progress, all);

                            //try to download next apk if exist
                            download();
                        } else if (status == ApkBaseLoader.STOP) {
                            Log.e(TAG, "(" + apkRequest.title + ") stop download");
                        }

                    }
                });
            }
        };

        mApkLoaderListener.put(apkRequest.uniqueID, downloadListener);

        CommonLogUtil.i(TAG, "download: start and bind service");
        Intent downloadService = new Intent();
        downloadService.setClass(mContext, ApkDownloadService.class);
        downloadService.putExtra(ApkDownloadService.EXTRA_UNIQUE_ID, apkRequest.uniqueID);
        mContext.startService(downloadService);
        mContext.bindService(downloadService, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public boolean hasInstallPermission() {
        CommonLogUtil.i(TAG, "hasInstallPermission: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return mContext.getPackageManager().canRequestPackageInstalls();
        } else {
            return true;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    public void requestInstallPermission() {
        CommonLogUtil.i(TAG, "requestInstallPermission: ");

        Uri packageURI = Uri.parse("package:" + mContext.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }


    @Override
    public void checkPermissionAndInstall(ApkRequest apkRequest) {
        CommonLogUtil.i(TAG, "checkPermissionAndInstall: ");

        Intent intent = new Intent();
        intent.setAction(ApkDownloadManager.ACTION_OFFER_INSTALL_START);
        intent.setPackage(mContext.getPackageName());
        intent.putExtra(ApkDownloadManager.RECEIVER_EXTRA_OFFER_ID, apkRequest.offerId);
        intent.putExtra(ApkDownloadManager.RECEIVER_EXTRA_CLICK_ID, apkRequest.clickId);
        mContext.sendBroadcast(intent);

        if (mInstallingPackageMap == null) {
            mInstallingPackageMap = new HashMap<>();
        }

        if (TextUtils.isEmpty(apkRequest.pkgName)) {
            String apkPath = getApkPath(apkRequest);
            if (!TextUtils.isEmpty(apkPath)) {
                apkRequest.pkgName = ApkResource.getApkPackageName(mContext, new File(apkPath));
            }
        }
        mInstallingPackageMap.put(apkRequest.pkgName, apkRequest);

        registerApkInstallBroadcastReceiver();

//        if(hasInstallPermission()) {
        install(apkRequest);
//        } else {
//            requestInstallPermission();
//        }
    }

    @Override
    public void install(ApkRequest apkRequest) {
        String apkPath = getApkPath(apkRequest);
        if (TextUtils.isEmpty(apkPath)) {
            return;
        }

        CommonLogUtil.i(TAG, "install: " + apkRequest.title);

        File apkFile = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Above 7.0 via FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".anythink.fileProvider", apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.parse("file://" + apkPath), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);

        AgentEventManager.onApkDownload(apkRequest.requestId, apkRequest.offerId, apkRequest.url, 4, null, 0, apkFile.length());
    }

    private String getApkPath(ApkRequest apkRequest) {
        return FileUtils.getResourcePath(apkRequest.uniqueID) + ApkBaseLoader.SUFFIX_APK;
    }

    @Override
    public void checkAndCleanApk() {

        try {
            String saveDirectory = FileUtils.getSaveDirectory();
            if (TextUtils.isEmpty(saveDirectory)) {
                return;
            }

            File saveDirectoryFile = new File(saveDirectory);
            File[] files = saveDirectoryFile.listFiles();
            if (files != null && files.length == 0) {
                return;
            }

            List<File> deleteFile = new ArrayList<>();
            long currentTimeMillis = System.currentTimeMillis();
            long expiredTimeMillis = mOfferCacheTime;
            for (File file : files) {
                if (file.getName().endsWith(ApkBaseLoader.SUFFIX_APK)) {// apk file
                    //check installed apk
                    if (ApkResource.isApkInstalled(mContext, file)) {
                        deleteFile.add(file);
                        continue;
                    }
                }

                if (file.lastModified() + expiredTimeMillis < currentTimeMillis) {
                    //check expired file
                    deleteFile.add(file);
                }
            }

            //clean expired file
            int size = deleteFile.size();
            for (int i = 0; i < size; i++) {
                Log.i(TAG, "clean expired file -> " + deleteFile.get(i).getName());
                deleteFile.get(i).delete();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleClick(ApkRequest apkRequest) {
        try {
            int pauseRequestCount = mPauseRequestMap.size();
            if (pauseRequestCount > 0) {//check apk is pausing
                ApkRequest pauseRequest = mPauseRequestMap.get(apkRequest.uniqueID);
                if (pauseRequest != null) {
                    mPauseRequestMap.remove(apkRequest.uniqueID);
                    pauseRequest.idle();
                    addQueue(pauseRequest);
                    download();
                }
            } else {
                if (isApkExist(apkRequest.uniqueID)) {//check apk whether exist
                    checkPermissionAndInstall(apkRequest);
                } else {
                    addQueue(apkRequest);
                    download();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClickNotification(String uniqueID, String url) {
        try {
            ApkRequest apkRequest;

            //download success -> start install
            if (mSuccessRequestMap != null) {
                apkRequest = mSuccessRequestMap.get(uniqueID);
                if (apkRequest != null) {
                    Log.i(TAG, "(" + apkRequest.title + ") onClickNotification: start intall");
                    ApkNotificationManager.getInstance(mContext).cancelNotification(apkRequest);
                    ApkNotificationManager.getInstance(mContext).showClickInstallNotification(apkRequest);
                    checkPermissionAndInstall(apkRequest);
                    return;
                }
            }

            //loading -> pause
            apkRequest = mDownloadingRequestMap.get(uniqueID);
            if (apkRequest != null) {
                if (apkRequest.isLoading()) {
                    Log.i(TAG, "(" + apkRequest.title + ") onClickNotification: pause download");
                    if (mDownloadBinder != null) {
                        mDownloadBinder.pause(apkRequest.uniqueID);
                    }
                    mPauseRequestMap.put(apkRequest.uniqueID, apkRequest);
                    return;
                }
            }

            //pause -> loading
            if (mDownloadingRequestMap.size() >= getDownloadCount()) {
                apkRequest = mPauseRequestMap.get(uniqueID);
                if (apkRequest != null) {
                    ApkNotificationManager.getInstance(mContext).cancelNotification(apkRequest);
                    ApkNotificationManager.getInstance(mContext).showNotification(apkRequest, apkRequest.progress, apkRequest.apkSize, true);
                } else {
                    int size = mRequestQueue.size();
                    for (int i = 0; i < size; i++) {
                        apkRequest = mRequestQueue.get(i);
                        if (TextUtils.equals(uniqueID, apkRequest.uniqueID)) {//apk is waiting for downloading
                            ApkNotificationManager.getInstance(mContext).showWaitingNotification(apkRequest);
                            break;
                        }
                    }
                }
                ApkDownloadManager.this.showToast("已有任务下载中");
                return;
            }

            apkRequest = mPauseRequestMap.get(uniqueID);
            if (apkRequest != null) {
                if (apkRequest.isPause()) {
                    Log.i(TAG, "(" + apkRequest.title + ") onClickNotification: resume download");
                    this.handleClick(apkRequest);
                    return;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCleanNotification(String uniqueID, String url) {
        try {
            ApkRequest apkRequest;

            //download success -> stop
            if (mSuccessRequestMap != null && mSuccessRequestMap.containsKey(uniqueID)) {
                apkRequest = mSuccessRequestMap.get(uniqueID);
                Log.i(TAG, "(" + apkRequest.title + ") onCleanNotification: download success");
                ApkNotificationManager.getInstance(mContext).cancelNotification(apkRequest);
                mSuccessRequestMap.remove(uniqueID);
                judgeAndStopService();
                return;
            }


            //pause -> stop
            apkRequest = mPauseRequestMap.get(uniqueID);
            if (apkRequest != null) {
                if (apkRequest.isPause()) {
                    if (mDownloadBinder != null) {
                        mDownloadBinder.stop(apkRequest.uniqueID);
                    }
                    mPauseRequestMap.remove(uniqueID);
                    Log.i(TAG, "(" + apkRequest.title + ") onCleanNotification: stop download");
                }
            }
            judgeAndStopService();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void judgeAndStopService() {
        try {
            if (mDownloadingRequestMap.size() != 0) {
                return;
            }
            if (mPauseRequestMap.size() != 0) {
                return;
            }
            if (mSuccessRequestMap != null && mSuccessRequestMap.size() != 0) {
                return;
            }

            if (mDownloadBinder != null && mDownloadBinder.canStopSelf()) {
                if (mServiceConnection != null) {
                    mContext.unbindService(mServiceConnection);
                    Intent downloadService = new Intent();
                    downloadService.setClass(mContext, ApkDownloadService.class);
                    mContext.stopService(downloadService);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void apkInstallSuccess(String packageName) {

        try {
            if (!mInstallingPackageMap.containsKey(packageName)) {
                return;
            }
            ApkRequest apkRequest = mInstallingPackageMap.get(packageName);
            if (apkRequest == null) {
                return;
            }

            String apkPath = getApkPath(apkRequest);//delete apk file
            if (!TextUtils.isEmpty(apkPath)) {
                new File(apkPath).delete();
            }

            mInstallingPackageMap.remove(packageName);
            mSuccessRequestMap.remove(apkRequest.uniqueID);
            ApkNotificationManager.getInstance(mContext).cancelNotification(apkRequest);

            Intent intent = new Intent();
            intent.setAction(ApkDownloadManager.ACTION_OFFER_INSTALL_SUCCESSFUL);
            intent.setPackage(mContext.getPackageName());
            intent.putExtra(ApkDownloadManager.RECEIVER_EXTRA_OFFER_ID, apkRequest.offerId);
            intent.putExtra(ApkDownloadManager.RECEIVER_EXTRA_CLICK_ID, apkRequest.clickId);
            mContext.sendBroadcast(intent);

            AgentEventManager.onApkDownload(apkRequest.requestId, apkRequest.offerId, apkRequest.url, 5, null, 0, 0);

            if (mInstallingPackageMap.size() == 0) {
                unRegisterApkInstallBroadcastReceiver();
            }
            judgeAndStopService();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void registerApkInstallBroadcastReceiver() {
        try {
            if (mApkInstallBroadcastReceiver != null) {
                return;
            }

            mApkInstallBroadcastReceiver = new ApkInstallBroadcaseReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_ADDED);
            filter.addDataScheme("package");

            mContext.registerReceiver(mApkInstallBroadcastReceiver, filter);

        } catch (Throwable e) {

        }
    }

    private void unRegisterApkInstallBroadcastReceiver() {
        try {
            if (mApkInstallBroadcastReceiver != null) {
                mContext.unregisterReceiver(mApkInstallBroadcastReceiver);
                mApkInstallBroadcastReceiver = null;
            }
        } catch (Throwable e) {

        }
    }


    //---------------------------------------------------------------------------------------------------------

    public Map<String, ApkRequest> getDownloadingRequestMap() {
        return mDownloadingRequestMap;
    }

    public ApkBaseLoader.DownloadListener getApkLoaderListener(String uniqueID) {
        return mApkLoaderListener.get(uniqueID);
    }

    private void showToast(final String text) {
        SDKContext.getInstance().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}