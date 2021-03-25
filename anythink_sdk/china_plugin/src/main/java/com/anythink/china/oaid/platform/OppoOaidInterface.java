/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.oaid.platform;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;

public interface OppoOaidInterface extends IInterface {
    public abstract static class OppoOaidBinder extends Binder implements OppoOaidInterface {
        public static OppoOaidInterface getOppoOaidInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            } else {
                try {
                    IInterface var1 = iBinder.queryLocalInterface("com.heytap.openid.IOpenID");
                    return (OppoOaidInterface)(var1 != null && var1 instanceof OppoOaidInterface ? (OppoOaidInterface)var1 : new OppoOaidInterfaceImpl(iBinder));
                } catch (Throwable var2) {
                    return null;
                }
            }
        }

        public static class OppoOaidInterfaceImpl implements OppoOaidInterface {
            public IBinder iBinder;

            public OppoOaidInterfaceImpl(IBinder iBinder) {
                this.iBinder = iBinder;
            }

            public final String getOaid(String var1, String var2, String var3) {
                String var4 = null;
                Parcel var5 = Parcel.obtain();
                Parcel var6 = Parcel.obtain();

                try {
                    var5.writeInterfaceToken("com.heytap.openid.IOpenID");
                    var5.writeString(var1);
                    var5.writeString(var2);
                    var5.writeString(var3);
                    this.iBinder.transact(1, var5, var6, 0);
                    var6.readException();
                    var4 = var6.readString();
                } catch (Exception var11) {
                    var11.printStackTrace();
                } finally {
                    var5.recycle();
                    var6.recycle();
                }

                return var4;
            }

            public final IBinder asBinder() {
                return this.iBinder;
            }
        }
    }
}