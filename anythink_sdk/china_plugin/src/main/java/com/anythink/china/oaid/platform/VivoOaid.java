/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.oaid.platform;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public final class VivoOaid {
    private Context context;
    private boolean c = false;
    String a = null;

    public VivoOaid(Context context) {
        this.context = context;
    }

    public final String getOaid() {
        String var1 = null;

        try {
            Uri var2 = Uri.parse("content://com.vivo.vms.IdProvider/IdentifierId/OAID");
            Cursor var3 = context.getContentResolver().query(var2, (String[]) null, (String) null, (String[]) null, (String) null);
            if (var3 != null) {
                if (var3.moveToNext()) {
                    var1 = var3.getString(var3.getColumnIndex("value"));
                }

                var3.close();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        } catch (Throwable var5) {
            var5.printStackTrace();
        }

        return var1;
    }
}
