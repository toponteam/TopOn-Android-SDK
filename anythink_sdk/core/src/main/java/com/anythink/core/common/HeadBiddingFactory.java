package com.anythink.core.common;

import android.content.Context;

import com.anythink.core.strategy.PlaceStrategy;

import java.lang.reflect.Constructor;
import java.util.List;

public class HeadBiddingFactory {
    private static final String HEADBIDDING_HANDLER_CLASS = "com.anythink.hb.ATHeadBiddingHandler";
    private static final String HEADBIDDING_S2S_HANDLER_CLASS = "com.anythink.hb.ATS2SHeadBiddingHandler";

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

    public static IHeadBiddingS2SHandler createS2SHeadBiddingHandler() {
        try {
            final Class<? extends IHeadBiddingS2SHandler> nativeClass = Class.forName(HEADBIDDING_S2S_HANDLER_CLASS)
                    .asSubclass(IHeadBiddingS2SHandler.class);
            final Constructor<?> nativeConstructor = nativeClass.getDeclaredConstructor((Class[]) null);
            nativeConstructor.setAccessible(true);
            return (IHeadBiddingS2SHandler) nativeConstructor.newInstance();
        } catch (Throwable e) {

        }
        return null;
    }

    public interface IHeadBiddingHandler {
        void setTestMode(boolean isTest);

        void initHbInfo(Context context, String requestId, String unitId, int format, long hbStartTime, long hbBidTimeout, List<PlaceStrategy.UnitGroupInfo> hbUnitInfoList);

        void startHeadBiddingRequest(IHeadBiddingCallback callback);
    }

    public interface IHeadBiddingS2SHandler {
        void setTestMode(boolean isTest);

        void initS2SHbInfo(Context context, String requestId, String placementId, int format, long hbBidTimeout, List<PlaceStrategy.UnitGroupInfo> hbUnitInfoList);

        void startS2SHbInfo(String bidUrl, IHeadBiddingCallback callback);
    }

    public interface IHeadBiddingCallback {

        void onSuccess(String requestId, List<PlaceStrategy.UnitGroupInfo> successList);

        void onFailed(String requestId, List<PlaceStrategy.UnitGroupInfo> failList);

        void onFinished(String requestId);

    }
}


