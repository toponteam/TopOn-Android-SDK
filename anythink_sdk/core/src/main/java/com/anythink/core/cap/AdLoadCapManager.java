/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.cap;

import android.content.Context;
import android.text.TextUtils;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.entity.AdLoadCapBean;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.SPUtil;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.concurrent.ConcurrentHashMap;

public class AdLoadCapManager {
    final String TAG = AdLoadCapManager.class.getSimpleName();

    ConcurrentHashMap<String, AdLoadCapBean> placementLoadCapMap = new ConcurrentHashMap<>();

    private static AdLoadCapManager sInstance;

    public synchronized static AdLoadCapManager getInstance() {
        if (sInstance == null) {
            sInstance = new AdLoadCapManager();
        }
        return sInstance;
    }


    public boolean isInLoadCapping(Context context, String placementId, PlaceStrategy placeStrategy) {

        if (placeStrategy.getLoadCap() <= 0) {
            return false;
        }

        AdLoadCapBean adLoadCapBean = placementLoadCapMap.get(placementId);

        if (adLoadCapBean == null) {
            String recordLoadCapString = SPUtil.getString(context, Const.SPU_PLACEMENT_LOAD_RECORD_NAME, placementId, "");
            adLoadCapBean = new AdLoadCapBean();
            if (!TextUtils.isEmpty(recordLoadCapString)) {
                adLoadCapBean.readCache(recordLoadCapString);
            }
            placementLoadCapMap.put(placementId, adLoadCapBean);
        }

        CommonLogUtil.i(TAG, "Load Cap info:" + placementId + ":" + adLoadCapBean.toString());

        if (adLoadCapBean.number >= placeStrategy.getLoadCap() && System.currentTimeMillis() - adLoadCapBean.loadTime <= placeStrategy.getLoadCapInterval()) {
            return true;
        }

        return false;

    }

    public void saveOneLoadTime(Context context, String placementId, PlaceStrategy placeStrategy) {
        AdLoadCapBean adLoadCapBean = placementLoadCapMap.get(placementId);

        if (adLoadCapBean == null) {
            String recordLoadCapString = SPUtil.getString(context, Const.SPU_PLACEMENT_LOAD_RECORD_NAME, placementId, "");
            adLoadCapBean = new AdLoadCapBean();
            if (!TextUtils.isEmpty(recordLoadCapString)) {
                adLoadCapBean.readCache(recordLoadCapString);
            }
            placementLoadCapMap.put(placementId, adLoadCapBean);
        }

        //Over the load cap interval
        if (System.currentTimeMillis() - adLoadCapBean.loadTime > placeStrategy.getLoadCapInterval()) {
            adLoadCapBean.loadTime = System.currentTimeMillis();
            adLoadCapBean.number = 0;
        }

        adLoadCapBean.number += 1;

        CommonLogUtil.i(TAG, "After save load cap:" + placementId + ":" + adLoadCapBean.toString());
        SPUtil.putString(context, Const.SPU_PLACEMENT_LOAD_RECORD_NAME, placementId, adLoadCapBean.toString());

    }

}
