package com.anythink.core.common.hb;


import com.anythink.core.common.entity.HiBidCache;

import java.util.concurrent.ConcurrentHashMap;

public class HeadBiddingCacheManager {

    private static HeadBiddingCacheManager sInstahce;
    //Key: AdsourceId
    ConcurrentHashMap<String, HiBidCache> cacheMap;

    private HeadBiddingCacheManager() {
        cacheMap = new ConcurrentHashMap<>();
    }

    public synchronized static HeadBiddingCacheManager getInstance() {
        if (sInstahce == null) {
            sInstahce = new HeadBiddingCacheManager();
        }
        return sInstahce;
    }

    public void addCache(String key, HiBidCache hiBidCache) {
        cacheMap.put(key, hiBidCache);
    }

    public void removeCache(String key) {
        cacheMap.remove(key);
    }

    public HiBidCache getCache(String key) {
        HiBidCache hiBidCache = cacheMap.get(key);
        if (hiBidCache != null && hiBidCache.outDateTime > System.currentTimeMillis()) {
            return hiBidCache;
        } else { //Remove it if out of date
            cacheMap.remove(key);
        }
        return null;
    }
}
