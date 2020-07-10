package com.anythink.china.common.resource;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.File;


public class ApkResource {

    public static String getApkPackageName(Context context, File apkFile) {
        if (context == null || apkFile == null) {
            return "";
        }

        try {
            PackageInfo packageArchiveInfo = context.getPackageManager().getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);

            return packageArchiveInfo.packageName;

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }


    public static boolean isApkInstalled(Context context, String pkgName) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(pkgName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isApkInstalled(Context context, File apkFile) {
        if (context == null || apkFile == null) {
            return false;
        }

        String apkPackageName = getApkPackageName(context, apkFile);
        if (!TextUtils.isEmpty(apkPackageName)) {
            return isApkInstalled(context, apkPackageName);
        }
        return false;
    }

    public static void openApp(Context context, String pkgName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
