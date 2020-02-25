package com.anythink.core.common;

import android.content.Context;

import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.strategy.PlaceStrategy;

import java.lang.reflect.Constructor;
import java.util.List;

public class HeadBiddingFactory {
    private static final String HEADBIDDING_HANDLER_CLASS = "com.anythink.hb.ATHeadBiddingHandler";

    public static IHeadBiddingHandler createHeadBiddingHandler() {
        try {
            final Class<? extends IHeadBiddingHandler> nativeClass = Class.forName(HEADBIDDING_HANDLER_CLASS)
                    .asSubclass(IHeadBiddingHandler.class);
            final Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
            nativeConstructor.setAccessible(true);
            return (IHeadBiddingHandler) nativeConstructor.newInstance();
        } catch (Throwable e) {

        }
        return null;
    }

    public interface IHeadBiddingHandler {
        void setTestMode(boolean isTest);

        void initHbInfo(Context context, String unitId, int format, List<PlaceStrategy.UnitGroupInfo> normalUnitInfoList, List<PlaceStrategy.UnitGroupInfo> hbUnitInfoList);

        void startHeadBiddingRequest(IHeadBiddingCallback callback);
    }

    public interface IHeadBiddingCallback {
        void onResultCallback(List<PlaceStrategy.UnitGroupInfo> resultList, List<PlaceStrategy.UnitGroupInfo> failList);
    }
}


