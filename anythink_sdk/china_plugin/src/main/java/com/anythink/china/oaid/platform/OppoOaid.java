/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.oaid.platform;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.Signature;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.anythink.china.oaid.OaidCallback;

import java.security.MessageDigest;


public final class OppoOaid {
    private Context context;
    OppoOaidInterface oppoOaidInterface;
    ServiceConnection c = new ServiceConnection() {
        public final void onServiceConnected(ComponentName var1, IBinder var2) {
            OppoOaid.this.oppoOaidInterface = OppoOaidInterface.OppoOaidBinder.getOppoOaidInterface(var2);
        }

        public final void onServiceDisconnected(ComponentName var1) {
            OppoOaid.this.oppoOaidInterface = null;
        }
    };

    public OppoOaid(Context var1) {
        this.context = var1;
    }

    public final String getOaid(OaidCallback callback) {
        String oaid = "";
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return "";
        } else {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.heytap.openid", "com.heytap.openid.IdentifyService"));
            intent.setAction("action.com.heytap.openid.OPEN_ID_SERVICE");
            if (this.context.bindService(intent, this.c, Context.BIND_AUTO_CREATE)) {
                try {
                    SystemClock.sleep(3000L);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }

                if (this.oppoOaidInterface != null) {
                    oaid = this.getOaid("OUID");
                    if (callback != null) {
                        callback.onSuccuss(oaid, false);
                    }
                }
            }

            if (TextUtils.isEmpty(oaid)) {
                if (callback != null) {
                    callback.onFail("Empty");
                }
            }

            return oaid;
        }
    }

    private String getOaid(String nameKey) {
        String customId = null;
        String var3 = null;
        String packageName = this.context.getPackageName();
        Signature[] var5;
        try {
            var5 = this.context.getPackageManager().getPackageInfo(packageName, 64).signatures;
        } catch (Exception var14) {
            var14.printStackTrace();
            var5 = null;
        }

        if (var5 != null && var5.length > 0) {
            byte[] var6 = var5[0].toByteArray();

            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
                if (messageDigest != null) {
                    byte[] digestData = messageDigest.digest(var6);
                    StringBuilder stringBuilder = new StringBuilder();
                    byte[] var10 = digestData;
                    int dataLength = digestData.length;

                    for (int i = 0; i < dataLength; ++i) {
                        byte var13 = var10[i];
                        stringBuilder.append(Integer.toHexString(var13 & 255 | 256).substring(1, 3));
                    }

                    var3 = stringBuilder.toString();
                }
            } catch (Exception var15) {
                var15.printStackTrace();
            }
        }


        customId = ((OppoOaidInterface.OppoOaidBinder.OppoOaidInterfaceImpl)this.oppoOaidInterface).getOaid(packageName, var3, nameKey);
        return customId;
    }
}
