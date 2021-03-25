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
import android.os.IBinder;

import com.anythink.china.oaid.OaidCallback;

import java.util.concurrent.LinkedBlockingQueue;

public final class SamsungOaid {
    private Context context;
    public final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue(1);
    ServiceConnection connection = new ServiceConnection() {
        public final void onServiceConnected(ComponentName var1, IBinder var2) {
            try {
                SamsungOaid.this.queue.put(var2);
            } catch (Exception var4) {
                var4.printStackTrace();
            }

        }

        public final void onServiceDisconnected(ComponentName var1) {
        }
    };

    public SamsungOaid(Context context) {
        this.context = context;
    }

    public final void getOaid(OaidCallback oaidCallback) {
        try {
            this.context.getPackageManager().getPackageInfo("com.samsung.android.deviceidservice", 0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setClassName("com.samsung.android.deviceidservice", "com.samsung.android.deviceidservice.DeviceIdService");
        boolean isSuccess = this.context.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
        if (isSuccess) {
            try {
                IBinder var4 = (IBinder) this.queue.take();
                SamsungInterface.SamsungInterfaceImpl var5 = new SamsungInterface.SamsungInterfaceImpl(var4);
                String var6 = var5.getOaid();
                if (oaidCallback != null) {
                    oaidCallback.onSuccuss(var6, false);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (oaidCallback != null) {
                    oaidCallback.onFail(throwable.getMessage());
                }
            }
        } else {
            if (oaidCallback != null) {
                oaidCallback.onFail("Service unbind.");
            }
        }

    }
}

