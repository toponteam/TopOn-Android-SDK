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

public class AsusInterface implements IInterface {
    private IBinder iBinder;

    public AsusInterface(IBinder var1) {
        this.iBinder = var1;
    }

    public final IBinder asBinder() {
        return this.iBinder;
    }

    public final String getOaid() {
        String var1 = null;
        Parcel var2 = Parcel.obtain();
        Parcel var3 = Parcel.obtain();

        try {
            var2.writeInterfaceToken("com.asus.msa.SupplementaryDID.IDidAidlInterface");
            this.iBinder.transact(3, var2, var3, 0);
            var3.readException();
            var1 = var3.readString();
        } catch (Throwable var5) {
            var2.recycle();
            var3.recycle();
            var5.printStackTrace();
        }

        var2.recycle();
        var3.recycle();
        return var1;
    }
}
