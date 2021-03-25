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
import android.os.RemoteException;

public interface HWIdentifierService extends IInterface {
    String getOaid() throws RemoteException;

    boolean isOaidTrackLimited() throws RemoteException;

    public abstract static class Stub extends Binder implements HWIdentifierService {
        private static final String DESCRIPTOR = "com.uodis.opendevice.aidl.OpenDeviceIdentifierService";
        static final int TRANSACTION_getOaid = 1;
        static final int TRANSACTION_isOaidTrackLimited = 2;

        public Stub() {
            this.attachInterface(this, "com.uodis.opendevice.aidl.OpenDeviceIdentifierService");
        }

        public static HWIdentifierService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            } else {
                IInterface iin = obj.queryLocalInterface("com.uodis.opendevice.aidl.OpenDeviceIdentifierService");
                return (HWIdentifierService)(iin != null && iin instanceof HWIdentifierService ? (HWIdentifierService)iin : new Stub.Proxy(obj));
            }
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch(code) {
                case 1:
                    data.enforceInterface("com.uodis.opendevice.aidl.OpenDeviceIdentifierService");
                    String _result = this.getOaid();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 2:
                    data.enforceInterface("com.uodis.opendevice.aidl.OpenDeviceIdentifierService");
                    boolean result = this.isOaidTrackLimited();
                    reply.writeNoException();
                    reply.writeInt(result ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString("com.uodis.opendevice.aidl.OpenDeviceIdentifierService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements HWIdentifierService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return "com.uodis.opendevice.aidl.OpenDeviceIdentifierService";
            }

            public String getOaid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();

                String _result;
                try {
                    _data.writeInterfaceToken("com.uodis.opendevice.aidl.OpenDeviceIdentifierService");
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }

            public boolean isOaidTrackLimited() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();

                boolean _result;
                try {
                    _data.writeInterfaceToken("com.uodis.opendevice.aidl.OpenDeviceIdentifierService");
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    _result = 0 != _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }

                return _result;
            }
        }
    }
}
