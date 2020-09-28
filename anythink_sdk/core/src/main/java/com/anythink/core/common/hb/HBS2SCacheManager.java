package com.anythink.core.common.hb;


import com.anythink.core.common.entity.HiBidCache;
import com.anythink.core.common.entity.S2SHBResponse;

import java.util.concurrent.ConcurrentHashMap;

public class HBS2SCacheManager {

    private static HBS2SCacheManager sInstahce;
    //Key: AdsourceId
    ConcurrentHashMap<String, S2SHBResponse> cacheMap;

    private HBS2SCacheManager() {
        cacheMap = new ConcurrentHashMap<>();
    }

    public static HBS2SCacheManager getInstance() {
        if (sInstahce == null) {
            sInstahce = new HBS2SCacheManager();
        }
        return sInstahce;
    }

    public void addCache(String key, S2SHBResponse hiBidCache) {
        cacheMap.put(key, hiBidCache);
    }

    public void removeCache(String key) {
        cacheMap.remove(key);
    }

    public S2SHBResponse getCache(String key) {
        S2SHBResponse s2sHbCache = cacheMap.get(key);
        if (s2sHbCache != null && s2sHbCache.outDateTime > System.currentTimeMillis()) {
            return s2sHbCache;
        } else { //Remove it if out of date
            cacheMap.remove(key);
        }
        return null;
    }
}
