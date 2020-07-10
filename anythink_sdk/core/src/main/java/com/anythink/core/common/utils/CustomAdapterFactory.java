package com.anythink.core.common.utils;

import android.util.Log;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.strategy.PlaceStrategy;

import java.lang.reflect.Constructor;

/**
 * Created by Z on 2018/1/9.
 * Adapter Factory
 */

public class CustomAdapterFactory {

    protected static CustomAdapterFactory instance = new CustomAdapterFactory();

    protected static AnyThinkBaseAdapter create(final String className) throws Exception {
        if (className != null) {
            final Class<? extends AnyThinkBaseAdapter> nativeClass = Class.forName(className)
                    .asSubclass(AnyThinkBaseAdapter.class);
            return instance.internalCreate(nativeClass);
        } else {
            return null;
        }
    }

    protected AnyThinkBaseAdapter internalCreate(
            final Class<? extends AnyThinkBaseAdapter> nativeClass) throws Exception {
        if (nativeClass == null) {
            Log.w(Const.RESOURCE_HEAD, "can not find adapter");
        }

        final Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
        nativeConstructor.setAccessible(true);
        return (AnyThinkBaseAdapter) nativeConstructor.newInstance();
    }

    public static AnyThinkBaseAdapter createAdapter(final PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        AnyThinkBaseAdapter customRewardVideoAdapter;

        try {
            customRewardVideoAdapter = CustomAdapterFactory.create(unitGroupInfo.adapterClassName);
        } catch (Throwable e) {

            e.printStackTrace();
            return null;
        }
        return customRewardVideoAdapter;
    }
}
