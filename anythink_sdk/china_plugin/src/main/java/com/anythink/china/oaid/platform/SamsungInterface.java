/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.oaid.platform;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;

public interface SamsungInterface extends IInterface {
    public static class SamsungInterfaceImpl implements SamsungInterface {
        private IBinder binder;

        public SamsungInterfaceImpl(IBinder var1) {
            this.binder = var1;
        }

        public final IBinder asBinder() {
            return this.binder;
        }

        public final String getOaid() {
            String oaid = null;
            Parcel var2 = Parcel.obtain();
            Parcel var3 = Parcel.obtain();

            try {
                var2.writeInterfaceToken("com.samsung.android.deviceidservice.IDeviceIdService");
                this.binder.transact(1, var2, var3, 0);
                var3.readException();
                oaid = var3.readString();
            } catch (Throwable var5) {
                var3.recycle();
                var2.recycle();
                var5.printStackTrace();
            }

            var3.recycle();
            var2.recycle();
            return oaid;
        }
    }
}
