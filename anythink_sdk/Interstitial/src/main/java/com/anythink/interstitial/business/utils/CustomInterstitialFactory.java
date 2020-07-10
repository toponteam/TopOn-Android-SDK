package com.anythink.interstitial.business.utils;

import android.util.Log;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.interstitial.unitgroup.api.CustomInterstitialAdapter;

import java.lang.reflect.Constructor;


public class CustomInterstitialFactory {
    protected static CustomInterstitialFactory instance = new CustomInterstitialFactory();

    public static CustomInterstitialAdapter create(final String className) throws Exception {
        if (className != null) {
            final Class<? extends CustomInterstitialAdapter> nativeClass = Class.forName(className)
                    .asSubclass(CustomInterstitialAdapter.class);
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

    protected CustomInterstitialAdapter internalCreate(
            final Class<? extends AnyThinkBaseAdapter> nativeClass) throws Exception {
        if (nativeClass == null) {
            Log.w(Const.RESOURCE_HEAD, "can not find native adapter");
        }

        final Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
        nativeConstructor.setAccessible(true);
        return (CustomInterstitialAdapter) nativeConstructor.newInstance();
    }
}
