/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.oaid.platform;


import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

public final class NubiaOaid {
    private Context context;

    public NubiaOaid(Context context) {
        this.context = context;
    }

    public final String getOaid() {
        String oaid = "";

        try {
            Uri uri = Uri.parse("content://cn.nubia.identity/identity");
            Bundle bundle = null;
            if (Build.VERSION.SDK_INT > 17) {
                ContentProviderClient var4 = this.context.getContentResolver().acquireContentProviderClient(uri);
                bundle = var4.call("getOAID", (String) null, (Bundle) null);
                if (var4 != null) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        var4.close();
                    } else {
                        var4.release();
                    }
                }
            } else {
                bundle = this.context.getContentResolver().call(uri, "getOAID", (String) null, (Bundle) null);
            }

            int code = -1;
            if (bundle != null) {
                code = bundle.getInt("code", -1);
            }

            if (code == 0) {
                oaid = bundle.getString("id");
                return oaid;
            }

            return oaid;
        } catch (Exception exception) {
            exception.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return oaid;
    }
}

