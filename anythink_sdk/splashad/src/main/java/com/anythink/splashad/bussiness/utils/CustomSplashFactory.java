package com.anythink.splashad.bussiness.utils;

import com.anythink.splashad.unitgroup.api.CustomSplashAdapter;

import java.lang.reflect.Constructor;

/**
 * Created by Z on 2018/1/9.
 */

public class CustomSplashFactory {
    protected static CustomSplashFactory instance = new CustomSplashFactory();

    public static CustomSplashAdapter create(final String className) throws Exception {
        if (className != null) {
            final Class<? extends CustomSplashAdapter> nativeClass = Class.forName(className)
                    .asSubclass(CustomSplashAdapter.class);
            return instance.internalCreate(nativeClass);
        } else {
            return null;
        }
    }

    protected CustomSplashAdapter internalCreate(
            final Class<? extends CustomSplashAdapter> nativeClass) throws Exception {

        final Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
        nativeConstructor.setAccessible(true);
        return (CustomSplashAdapter) nativeConstructor.newInstance();
    }
}
