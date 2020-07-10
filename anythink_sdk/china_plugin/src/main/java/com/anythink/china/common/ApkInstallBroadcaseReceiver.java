package com.anythink.china.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.anythink.core.common.utils.CommonLogUtil;

public class ApkInstallBroadcaseReceiver extends BroadcastReceiver {

    private static final String TAG = ApkInstallBroadcaseReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            if (intent.getData() != null) {
                String packageName = intent.getData().getSchemeSpecificPart();
                CommonLogUtil.i(TAG, "onReceive: apk install success( " + packageName + ")");
                ApkDownloadManager.getInstance(context).apkInstallSuccess(packageName);
            }
        }
    }
}
