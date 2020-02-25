package com.anythink.core.common.utils;

import android.text.TextUtils;
import android.util.Log;

import com.anythink.core.common.base.Const;

import java.util.regex.Pattern;

/**
 * Created by Ivan on 2015/1/7.
 */
public class CommonSDKUtil {

    public static class AppStoreUtils {
        /**
         * URI format: {scheme}://{host}/{path}?{params}
         */
        public static final String PACKAGE_NAME_GOOGLE_PLAY = "com.android.vending";//

    }

    public static boolean isVailScenario(String scenario) {
        String regex = "^[A-Za-z0-9]+$";
        if (!TextUtils.isEmpty(scenario) && scenario.length() == 14) {
            boolean isMatch = Pattern.matches(regex, scenario);
            if (isMatch) {
                return true;
            } else {
                Log.e(Const.RESOURCE_HEAD, "Invail Scenario(" + scenario + "):Scenario contains some characters that are not in the [A-Za-z0-9]");
                return false;
            }

        } else {
            Log.e(Const.RESOURCE_HEAD, "Invail Scenario(" + scenario + "):Scenario'length isn't 14");
            return false;
        }
    }

}
