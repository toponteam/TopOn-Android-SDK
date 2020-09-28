package com.anythink.core.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by Administrator on 2016/5/17.
 */
public class SPUtil {

    public static void clear(Context context, String name) {
        if (context == null) {
            return;
        }
        try {
            SharedPreferences dayupdate = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            dayupdate.edit().clear().apply();
        } catch (Exception e) {

        } catch (Error e) {

        }

    }

    public static void putLong(Context context, String name, String key, long value) {
        if (context == null) {
            return;
        }
        try {
            SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(key, value);
            editor.apply();
        } catch (Exception e) {
        } catch (Error e) {
        }
    }

    public static void putString(Context context, String name, String key, String value) {
        if (context == null) {
            return;
        }
        try {
            SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, String.valueOf(value));
            editor.apply();
        } catch (Exception e) {
        } catch (Error e) {
        }
    }

    public static void putInt(Context context, String name, String key, int value) {
        if (context == null) {
            return;
        }
        try {
            SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key, value);
            editor.apply();
        } catch (Exception e) {
        } catch (Error e) {
        }
    }

    public static Long getLong(Context context, String name, String key, Long defut) {
        if (context == null) {
            return 0L;
        }
        try {
            SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            return sp.getLong(key, defut);
        } catch (Error e) {

        } catch (Exception e) {

        }
        return defut;

    }

    public static int getInt(Context context, String name, String key, int defut) {
        if (context == null) {
            return defut;
        }
        try {
            SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            return sp.getInt(key, defut);
        } catch (Exception e) {

        } catch (Error e) {

        }
        return defut;

    }

    public static String getString(Context context, String name, String key, String defut) {
        if (context == null) {
            return null;
        }
        try {
            SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
            return sp.getString(key, defut);
        } catch (Exception e) {

        } catch (Error e) {

        }
        return defut;

    }

}
