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

public final class ZuiOaid {
    private Context context;
    ZuiInterface zuiInterface;
    ServiceConnection connection = new ServiceConnection() {
        public final void onServiceConnected(ComponentName var1, IBinder var2) {
            ZuiOaid.this.zuiInterface = new ZuiInterface.ZuiInterfaceImpl(var2);
        }

        public final void onServiceDisconnected(ComponentName var1) {
        }
    };

    public ZuiOaid(Context context) {
        this.context = context;
    }

    public final void getOaid(OaidCallback callback) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.zui.deviceidservice", "com.zui.deviceidservice.DeviceidService");
            boolean isBindSuccess = this.context.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
            if (isBindSuccess && this.zuiInterface != null) {
                String var6 = this.zuiInterface.getOaid();
                if (callback != null) {
                    callback.onSuccuss(var6, false);
                }
            } else {
                if (callback != null) {
                    callback.onFail("Service unbind");
                }
            }
        } catch (Throwable throwable) {
            if (callback != null) {
                callback.onFail(throwable.getMessage());
            }
        }

    }
}
