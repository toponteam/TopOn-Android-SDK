/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.common.download;

import android.content.Context;

import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;

public interface IApkManager {

    /**
     * Entry, call handleClick() to start download apk
     */
    void realStartDownloadApp(final Context context, final BaseAdRequestInfo baseAdRequestInfo
            , final BaseAdContent baseAdContent, final String url, final String clickId, Runnable runnableBeforeStartDownload);

    void addQueue(ApkRequest apkRequest);

    int getDownloadCount();

    boolean isApkExist(String uniqueID);

    void download();

    boolean hasInstallPermission();

    void requestInstallPermission();

    void checkPermissionAndInstall(ApkRequest apkRequest);

    void install(ApkRequest apkRequest);

    void checkAndCleanApk();

    void handleClick(ApkRequest apkRequest);

    void onClickNotification(String uniqueID, String url);

    void onCleanNotification(String uniqueID, String url);
}
