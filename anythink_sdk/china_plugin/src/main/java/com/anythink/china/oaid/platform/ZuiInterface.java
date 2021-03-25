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

public interface ZuiInterface extends IInterface {
    String getOaid();

    String getOaid(String var1);

    public static class ZuiInterfaceImpl implements ZuiInterface {
        private IBinder iBinder;

        public ZuiInterfaceImpl(IBinder var1) {
            this.iBinder = var1;
        }

        public final IBinder asBinder() {
            return null;
        }

        public final String getOaid() {
            String var1 = null;
            Parcel var2 = Parcel.obtain();
            Parcel var3 = Parcel.obtain();

            try {
                var2.writeInterfaceToken("com.zui.deviceidservice.IDeviceidInterface");
                this.iBinder.transact(1, var2, var3, 0);
                var3.readException();
                var1 = var3.readString();
                String var4 = var1;
                return var4;
            } catch (Exception var8) {
                var8.printStackTrace();
            } finally {
                var3.recycle();
                var2.recycle();
            }

            return var1;
        }

        public final String getOaid(String var1) {
            String var2 = null;
            Parcel var3 = Parcel.obtain();
            Parcel var4 = Parcel.obtain();

            try {
                var3.writeInterfaceToken("com.zui.deviceidservice.IDeviceidInterface");
                this.iBinder.transact(4, var3, var4, 0);
                var4.readException();
                var2 = var4.readString();
                String var5 = var2;
                return var5;
            } catch (Exception var9) {
                var9.printStackTrace();
            } finally {
                var4.recycle();
                var3.recycle();
            }

            return var2;
        }
    }
//    }
}
