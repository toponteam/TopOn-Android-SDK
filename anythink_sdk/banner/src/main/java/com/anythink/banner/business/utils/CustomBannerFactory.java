package com.anythink.banner.business.utils;

import com.anythink.banner.unitgroup.api.CustomBannerAdapter;

import java.lang.reflect.Constructor;

/**
 * Created by Z on 2018/1/9.
 * BannerAdpter Factory
 */

public class CustomBannerFactory {
    protected static CustomBannerFactory instance = new CustomBannerFactory();

    public static CustomBannerAdapter create(final String className) throws Exception {
        if (className != null) {
            final Class<? extends CustomBannerAdapter> nativeClass = Class.forName(className)
                    .asSubclass(CustomBannerAdapter.class);
            return instance.internalCreate(nativeClass);
        } else {
            return null;
        }
    }

//    @Deprecated // for testing
//    public static void setInstance(
//            final CustomEventNativeFactory customEventNativeFactory) {
//        Preconditions.checkNotNull(customEventNativeFactory);
//
//        instance = customEventNativeFactory;
//    }

    protected CustomBannerAdapter internalCreate(
            final Class<? extends CustomBannerAdapter> nativeClass) throws Exception {
        final Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
        nativeConstructor.setAccessible(true);
        return (CustomBannerAdapter) nativeConstructor.newInstance();
    }
}
