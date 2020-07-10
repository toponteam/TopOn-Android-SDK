package com.anythink.china.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.anythink.china.common.PermissionRequestManager;
import com.anythink.china.oaid.OaidAidlUtil;
import com.anythink.china.oaid.OaidCallback;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.SPUtil;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class ChinaDeviceUtils {

    private static String mac = "";
    private static String imei = "";
    private static String oaid = "";


    public static void initDeviceInfo(final Context context) {
        String spuOaid = SPUtil.getString(context, Const.SPU_NAME, "oaid", "");
        if (TextUtils.isEmpty(oaid)) {
            initOaid(context);
            if (TextUtils.isEmpty(oaid)) {
                OaidAidlUtil oaidAidlUtil = new OaidAidlUtil(context);
                oaidAidlUtil.getOaid(new OaidCallback() {

                    @Override
                    public void onSuccuss(String oaid, boolean isOaidTrackLimited) {
                        //check oaid status
                        if (isInvailOaid(oaid)) {
                            return;
                        }
                        ChinaDeviceUtils.oaid = oaid;
                        SPUtil.putString(context, Const.SPU_NAME, "oaid", oaid);
                    }

                    @Override
                    public void onFail(String errMsg) {

                    }
                });
            }
        } else {
            oaid = spuOaid;
        }

        mac = MacUtils.getMac(context);
        imei = ImeiUtils.getIMEI(context);

    }

    private static boolean isInvailOaid(String oaid) {
        return Pattern.matches("^[0-]+$", oaid);
    }

    public static String getMac() {
        return mac;
    }

    public static String getImei(Context context) {
        if (TextUtils.isEmpty(imei) && PermissionRequestManager.checkPermissionGrant(context, PermissionRequestManager.READ_PHONE_STATE_PERMISSION)) {
            imei = ImeiUtils.getIMEI(context);
        }
        return imei;
    }

    public static String getOaid() {
        return oaid;
    }

    public static String initOaid(Context context) {
        if (!TextUtils.isEmpty(oaid)) {
            return oaid;
        }
        try {
            oaid = new a(context).b;
            return oaid;
        } catch (Throwable t) {
            return "";
        }
    }

    @SuppressLint({"PrivateApi"})
    static final class a {
        private static Object e;
        private static Class<?> f;
        private static Method g;
        private static Method h;
        private static Method i;
        private static Method j;
        final String a;
        final String b;
        final String c;
        final String d;

        static {
            g = null;
            h = null;
            i = null;
            j = null;
            try {
                f = Class.forName("com.android.id.impl.IdProviderImpl");
                e = f.newInstance();
                g = f.getMethod("getUDID", new Class[]{Context.class});
                h = f.getMethod("getOAID", new Class[]{Context.class});
                i = f.getMethod("getVAID", new Class[]{Context.class});
                j = f.getMethod("getAAID", new Class[]{Context.class});
            } catch (Throwable e) {

            }
        }

        a(Context context) {
            this.a = a(context, g);
            this.b = a(context, h);
            this.c = a(context, i);
            this.d = a(context, j);
        }

        static boolean a() {
            return (f == null || e == null) ? false : true;
        }

        private static String a(Context context, Method method) {
            if (!(e == null || method == null)) {
                try {
                    Object invoke = method.invoke(e, new Object[]{context});
                    if (invoke != null) {
                        return (String) invoke;
                    }
                } catch (Throwable e) {

                }
            }
            return null;
        }
    }
}
