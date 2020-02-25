package com.anythink.rewardvideo.bussiness.utils;

import android.util.Log;

import com.anythink.core.common.base.Const;
import com.anythink.rewardvideo.unitgroup.api.CustomRewardVideoAdapter;

import java.lang.reflect.Constructor;

/**
 * Created by Z on 2018/1/9.
 */

public class CustomRewardVideoFactory {
    protected static CustomRewardVideoFactory instance = new CustomRewardVideoFactory();

    public static CustomRewardVideoAdapter create(final String className) throws Exception {
        if (className != null) {
            final Class<? extends CustomRewardVideoAdapter> nativeClass = Class.forName(className)
                    .asSubclass(CustomRewardVideoAdapter.class);
            return instance.internalCreate(nativeClass);
        } else {
            return null;
        }
    }


    protected CustomRewardVideoAdapter internalCreate(
            final Class<? extends CustomRewardVideoAdapter> nativeClass) throws Exception {
        if (nativeClass == null) {
            Log.w(Const.RESOURCE_HEAD, "can not find adapter");
        }

        final Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
        nativeConstructor.setAccessible(true);
        return (CustomRewardVideoAdapter) nativeConstructor.newInstance();
    }
}
