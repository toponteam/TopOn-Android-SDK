/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.oaid.platform;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.anythink.china.oaid.OaidCallback;

public final class MeizuOaid {
    private Context context;

    public MeizuOaid(Context context) {
        this.context = context;
    }

    public final void getOaid(OaidCallback oaidCallback) {
        try {
            this.context.getPackageManager().getPackageInfo("com.meizu.flyme.openidsdk", 0);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Uri uri = Uri.parse("content://com.meizu.flyme.openidsdk/");
        ContentResolver resolver = this.context.getContentResolver();
        String oaid = null;
        String errorMsg = "Empty";
        try {
            Cursor cursor = resolver.query(uri, (String[]) null, (String) null, new String[]{"oaid"}, (String) null);
            String value = null;

            if (cursor == null) {
                oaid = null;
            } else if (cursor.isClosed()) {
                oaid = null;
            } else {
                cursor.moveToFirst();
                int valueIndex = cursor.getColumnIndex("value");
                if (valueIndex > 0) {
                    value = cursor.getString(valueIndex);
                }

                oaid = value;
            }

            if (oaidCallback != null) {
                oaidCallback.onSuccuss(oaid, false);
            }

            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable throwable) {
            errorMsg = throwable.getMessage();
        }

        if (TextUtils.isEmpty(oaid)) {
            if (oaidCallback != null) {
                oaidCallback.onFail(errorMsg);
            }
        }

    }
}
