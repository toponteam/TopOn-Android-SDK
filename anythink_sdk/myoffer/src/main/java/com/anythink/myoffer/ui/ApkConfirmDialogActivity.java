package com.anythink.myoffer.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.myoffer.buiness.MyOfferAdManager;

public class ApkConfirmDialogActivity extends Activity {

    public static MyOfferAd myOfferAd;
    public static MyOfferSetting myOfferSetting;
    public static String requestId;
    public static String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            AlertDialog comfirmDialog = new AlertDialog.Builder(this)
                    .setTitle("下载")
                    .setMessage("立即下载\"" + myOfferAd.getTitle() + "\"?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MyOfferAdManager.getInstance(getApplicationContext()).realStartDownloadApp(requestId, myOfferSetting, myOfferAd, url);
                            finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .create();

            comfirmDialog.show();
        } catch (Throwable e) {
            finish();
        }
    }

    public static void start(Context context, String requestId, MyOfferSetting myOfferSetting, MyOfferAd myOfferAd, String url) {
        ApkConfirmDialogActivity.requestId = requestId;
        ApkConfirmDialogActivity.myOfferSetting = myOfferSetting;
        ApkConfirmDialogActivity.myOfferAd = myOfferAd;
        ApkConfirmDialogActivity.url = url;

        Intent intent = new Intent(context, ApkConfirmDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == keyCode) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        ApkConfirmDialogActivity.requestId = null;
        ApkConfirmDialogActivity.myOfferSetting = null;
        ApkConfirmDialogActivity.myOfferAd = null;
        ApkConfirmDialogActivity.url = null;

        super.onDestroy();
    }
}
