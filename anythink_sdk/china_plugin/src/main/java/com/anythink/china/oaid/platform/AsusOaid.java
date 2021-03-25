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

public final class AsusOaid {
    private Context context;
    public final LinkedBlockingQueue<IBinder> blockingQueue = new LinkedBlockingQueue(1);
    ServiceConnection connection = new ServiceConnection() {
        public final void onServiceConnected(ComponentName var1, IBinder var2) {
            try {
                AsusOaid.this.blockingQueue.put(var2);
            } catch (Throwable var4) {
                var4.printStackTrace();
            }

        }

        public final void onServiceDisconnected(ComponentName var1) {
        }
    };

    public AsusOaid(Context context) {
        this.context = context;
    }

    public final void getOaid(OaidCallback callback) {
        try {
            this.context.getPackageManager().getPackageInfo("com.asus.msa.SupplementaryDID", 0);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        Intent intent = new Intent();
        intent.setAction("com.asus.msa.action.ACCESS_DID");
        ComponentName componentName = new ComponentName("com.asus.msa.SupplementaryDID", "com.asus.msa.SupplementaryDID.SupplementaryDIDService");
        intent.setComponent(componentName);
        boolean isBindSuccess = this.context.bindService(intent, this.connection, 1);
        if (isBindSuccess) {
            try {
                IBinder iBinder = (IBinder)this.blockingQueue.take();
                AsusInterface asusInterface = new AsusInterface(iBinder);
                String oaid = asusInterface.getOaid();
                if (callback != null) {
                    callback.onSuccuss(oaid, false);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onFail(e.getMessage());
                }
            } catch (Throwable t) {
                if (callback != null) {
                    callback.onFail(t.getMessage());
                }
            }
        } else {
            if (callback != null) {
                callback.onFail("Empty");
            }
        }

    }
}
