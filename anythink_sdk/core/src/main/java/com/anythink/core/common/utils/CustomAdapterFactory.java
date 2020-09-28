package com.anythink.core.common.utils;

import android.util.Log;

import com.anythink.core.api.ATBaseAdAdapter;
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

    public static ATBaseAdAdapter create(final String className) throws Exception {
        if (className != null) {
            final Class<? extends ATBaseAdAdapter> nativeClass = Class.forName(className)
                    .asSubclass(ATBaseAdAdapter.class);
            return instance.internalCreate(nativeClass);
        } else {
            return null;
        }
    }

    protected ATBaseAdAdapter internalCreate(
            final Class<? extends AnyThinkBaseAdapter> nativeClass) throws Exception {
        if (nativeClass == null) {
            Log.w(Const.RESOURCE_HEAD, "can not find adapter");
        }

        final Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
        nativeConstructor.setAccessible(true);
        return (ATBaseAdAdapter) nativeConstructor.newInstance();
    }

    public static ATBaseAdAdapter createAdapter(final PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        ATBaseAdAdapter customRewardVideoAdapter;

        try {
            customRewardVideoAdapter = CustomAdapterFactory.create(unitGroupInfo.adapterClassName);
        } catch (Throwable e) {

            e.printStackTrace();
            return null;
        }
        return customRewardVideoAdapter;
    }
}
