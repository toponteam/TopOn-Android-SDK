/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.common.download;

public interface IApkManager {

    void addQueue(ApkRequest apkRequest);

    int getDownloadCount();

    boolean isApkExist(String url);

    void download();

    boolean hasInstallPermission();

    void requestInstallPermission();

    void checkPermissionAndInstall(ApkRequest apkRequest);

    void install(ApkRequest apkRequest);

    void checkAndCleanApk();

    void handleClick(ApkRequest apkRequest);

    void onClickNotification(String url);

    void onCleanNotification(String url);
}
