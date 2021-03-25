/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.oaid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;


import com.anythink.china.api.ATChinaSDKHandler;
import com.anythink.china.api.OaidSDKCallbackListener;
import com.anythink.china.common.download.task.TaskManager;
import com.anythink.china.oaid.platform.AsusOaid;
import com.anythink.china.oaid.platform.HWOaidAidlUtil;
import com.anythink.china.oaid.platform.MeizuOaid;
import com.anythink.china.oaid.platform.NubiaOaid;
import com.anythink.china.oaid.platform.OppoOaid;
import com.anythink.china.oaid.platform.SamsungOaid;
import com.anythink.china.oaid.platform.VivoOaid;
import com.anythink.china.oaid.platform.ZuiOaid;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class OaidObtainUtil {

    /**
     * Get Oaid Info
     *
     * @param context
     * @param oaidCallback
     */
    public static void initOaidInfo(Context context, OaidCallback oaidCallback) {
        String oaid = "";
        try {
            oaid = initOaidBySystem(context);
            if (!TextUtils.isEmpty(oaid)) {
                if (oaidCallback != null) {
                    oaidCallback.onSuccuss(oaid, false);
                }
                return;
            }

            String manufacturer = Build.MANUFACTURER;
            if (isFreeMeOS()) {
                manufacturer = "FERRMEOS";
            } else if (isSSUIOS()) {
                manufacturer = "SSUI";
            }

            if (!TextUtils.isEmpty(manufacturer)) {
                manufacturer = manufacturer.toUpperCase();
                List<String> manufacturerList = Arrays.asList("ASUS", "HUAWEI", "OPPO", "ONEPLUS", "ZTE", "FERRMEOS", "SSUI", "SAMSUNG", "MEIZU", "MOTOLORA", "LENOVO");
                if (manufacturerList.contains(manufacturer)) {
                    asyncGetOaid(context, manufacturer, oaidCallback);
                } else if ("VIVO".equals(manufacturer)) {
                    oaid = (new VivoOaid(context)).getOaid();
                } else if ("NUBIA".equals(manufacturer)) {
                    oaid = (new NubiaOaid(context)).getOaid();
                } else {
                    initOaidByOaidSDK(context, oaidCallback);
                }
            }
        } catch (Throwable e) {
        }

        if (!TextUtils.isEmpty(oaid)) {
            if (oaidCallback != null) {
                oaidCallback.onSuccuss(oaid, false);
            }
        }
    }

    private static boolean isFreeMeOS() {
        String name = getProperty("ro.build.freeme.label");
        return !TextUtils.isEmpty(name) && name.equalsIgnoreCase("FREEMEOS");
    }

    private static boolean isSSUIOS() {
        String name = getProperty("ro.ssui.product");
        return !TextUtils.isEmpty(name) && !name.equalsIgnoreCase("unknown");
    }

    private static String getProperty(String value) {
        String propertyInfo = null;
        if (value == null) {
            return null;
        } else {
            try {
                Class var2 = Class.forName("android.os.SystemProperties");
                Method var3 = var2.getMethod("get", String.class, String.class);
                propertyInfo = (String) var3.invoke(var2, value, "unknown");
            } catch (Exception var4) {
            }

            return propertyInfo;
        }
    }

    private static void asyncGetOaid(final Context context, final String manufacturer, final OaidCallback oaidCallback) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            public final void run() {
                OaidCallback innerCallback = new OaidCallback() {
                    @Override
                    public void onSuccuss(String oaid, boolean isOaidTrackLimited) {
                        if (oaidCallback != null) {
                            oaidCallback.onSuccuss(oaid, isOaidTrackLimited);
                        }
                    }

                    @Override
                    public void onFail(String errMsg) {
                        initOaidByOaidSDK(context, oaidCallback);
                    }
                };

                try {
                    switch (manufacturer) {
                        case "ASUS":
                            (new AsusOaid(context)).getOaid(innerCallback);
                            break;
                        case "OPPO":
                        case "ONEPLUS":
                            (new OppoOaid(context)).getOaid(innerCallback);
                            break;

                        case "ZTE":
                        case "FERRMEOS":
                        case "SSUI":
                            initOaidByOaidSDK(context, oaidCallback);
                            break;

                        case "HUAWEI":
                            new HWOaidAidlUtil(context).getOaid(innerCallback);
                            break;
                        case "SAMSUNG":
                            (new SamsungOaid(context)).getOaid(innerCallback);
                            break;

                        case "LENOVO":
                        case "MOTOLORA":
                            (new ZuiOaid(context)).getOaid(innerCallback);
                            break;

                        case "MEIZU":
                            (new MeizuOaid(context)).getOaid(innerCallback);
                            break;
                        default:
                            initOaidByOaidSDK(context, oaidCallback);
                            break;
                    }
                } catch (Throwable e) {
                    if (oaidCallback != null) {
                        oaidCallback.onFail(e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Get Oaid By SDK
     *
     * @param context
     */
    private static void initOaidByOaidSDK(final Context context, final OaidCallback oaidCallback) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                try {
                    ATChinaSDKHandler.handleInitOaidSDK(context.getApplicationContext(), new OaidSDKCallbackListener() {
                        @Override
                        public void OnSupport(boolean b, com.bun.miitmdid.interfaces.IdSupplier idSupplier) {
                            String oaid = idSupplier != null ? idSupplier.getOAID() : "";
                            if (!TextUtils.isEmpty(oaid)) {
                                if (oaidCallback != null) {
                                    oaidCallback.onSuccuss(oaid, false);
                                }
                            } else {
                                if (oaidCallback != null) {
                                    oaidCallback.onFail("No Support Oaid.");
                                }
                            }
                        }
                    });
                } catch (Throwable e) {

                }
            }
        });

    }

    private static String initOaidBySystem(Context context) {
        try {
            String oaid = new IdProvider(context).b;
            return oaid;
        } catch (Throwable t) {
            return "";
        }
    }

    @SuppressLint({"PrivateApi"})
    static final class IdProvider {
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

        IdProvider(Context context) {
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
