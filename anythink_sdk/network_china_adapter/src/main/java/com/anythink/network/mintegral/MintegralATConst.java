package com.anythink.network.mintegral;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by zhou on 2018/7/4.
 */

public class MintegralATConst {
    public static final int NETWORK_FIRM_ID = 6;
    @Deprecated
    public static final String AUTO_RENDER_NATIVE_WIDTH = "mintegral_auto_render_native_width";
    @Deprecated
    public static final String AUTO_RENDER_NATIVE_HEIGHT = "mintegral_auto_render_native_height";

    public static String getNetworkVersion() {
        try {
            Class mtgConfiguration = Class.forName("com.mintegral.msdk.out.MTGConfiguration");
            for (Field field : mtgConfiguration.getFields()) {
                field.setAccessible(true);
                if (field.getType().toString().endsWith("java.lang.String") && Modifier.isStatic(field.getModifiers())) {
                    String verisonName = field.get(mtgConfiguration).toString();
                    if (verisonName.startsWith("MAL")) {
                        return verisonName;
                    }
                }

            }
        } catch (Throwable e) {

        }
        return "";
    }

    public static boolean isChinaSdk() {
        String networkVersion = getNetworkVersion();
        return !TextUtils.isEmpty(networkVersion) && networkVersion.endsWith("2");
    }
}
