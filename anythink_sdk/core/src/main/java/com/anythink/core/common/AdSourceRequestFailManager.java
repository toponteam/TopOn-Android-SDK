package com.anythink.core.common;

import android.content.Context;

import com.anythink.core.strategy.PlaceStrategy;

import java.util.concurrent.ConcurrentHashMap;

public class AdSourceRequestFailManager {

    private static AdSourceRequestFailManager sIntance;


    ConcurrentHashMap<String, Long> concurrentHashMap;

    public synchronized static AdSourceRequestFailManager getInstance() {
        if (sIntance == null) {
            sIntance = new AdSourceRequestFailManager();
        }
        return sIntance;
    }

    private AdSourceRequestFailManager() {
        concurrentHashMap = new ConcurrentHashMap<String, Long>();
    }


    public boolean isInRequestFailInterval(PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        if (unitGroupInfo.getRequestFailInterval() == 0) {
            return false;
        }

        long requestFailTime = concurrentHashMap.get(unitGroupInfo.unitId) != null ? concurrentHashMap.get(unitGroupInfo.unitId) : 0L;
        if (requestFailTime + unitGroupInfo.getRequestFailInterval() < System.currentTimeMillis()) {
            return false;
        }

        return true;
    }

    public void putAdSourceRequestFailTime(String adsourceId, long failTime) {
        concurrentHashMap.put(adsourceId, failTime);
    }
}
