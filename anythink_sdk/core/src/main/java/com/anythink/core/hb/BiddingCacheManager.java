/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb;


import android.text.TextUtils;

import com.anythink.core.api.MediationBidManager;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BiddingResult;
import com.anythink.core.common.utils.SPUtil;


import java.util.concurrent.ConcurrentHashMap;

public class BiddingCacheManager {
    private static BiddingCacheManager sInstahce;
    //Key: AdsourceId
    ConcurrentHashMap<String, BiddingResult> cacheMap;

    private MediationBidManager mMediationBidManager;

    private BiddingCacheManager() {
        cacheMap = new ConcurrentHashMap<>();
    }

    public static BiddingCacheManager getInstance() {
        if (sInstahce == null) {
            sInstahce = new BiddingCacheManager();
        }
        return sInstahce;
    }

    public void addCache(String adsourceId, BiddingResult hiBidCache) {
        cacheMap.put(adsourceId, hiBidCache);
        /**
         * Adx Network will save in xml
         */
        if (hiBidCache.networkFirmId == Const.NETWORK_FIRM.ADX_NETWORK_FIRM_ID) {
            SPUtil.putString(SDKContext.getInstance().getContext(), Const.HB_CACHE_FILE, adsourceId, hiBidCache.toFileCacheString());
        }
    }

    public void removeCache(String adsourceId, int networkFirmId) {
        cacheMap.remove(adsourceId);
        /**
         * Adx Network will remove in xml
         */
        if (networkFirmId == Const.NETWORK_FIRM.ADX_NETWORK_FIRM_ID) {
            SPUtil.remove(SDKContext.getInstance().getContext(), Const.HB_CACHE_FILE, adsourceId);
        }
    }

    public BiddingResult getCache(String adsourceId, int networkFirmId) {
        BiddingResult s2sHbCache = cacheMap.get(adsourceId);

        /**
         * Adx Network will remove in xml
         */
        if (s2sHbCache == null && networkFirmId == Const.NETWORK_FIRM.ADX_NETWORK_FIRM_ID) {
            String hbCache = SPUtil.getString(SDKContext.getInstance().getContext(), Const.HB_CACHE_FILE, adsourceId, "");
            if (!TextUtils.isEmpty(hbCache)) {
                s2sHbCache = BiddingResult.parseJSONString(hbCache);
            }
            if (s2sHbCache != null) {
                cacheMap.put(adsourceId, s2sHbCache);
            }
        }
        return s2sHbCache;
    }


    public void setMediationBidManager(MediationBidManager mediationBidManager) {
        mMediationBidManager = mediationBidManager;
    }

    public MediationBidManager getMediationBidManager() {
        return mMediationBidManager;
    }
}
