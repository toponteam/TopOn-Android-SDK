/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.cap;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.utils.SPUtil;
import com.anythink.core.common.utils.task.TaskManager;
import com.anythink.core.strategy.PlaceStrategy;

public class AdPacingManager {

    private static AdPacingManager sInstance;

    private final String PACING_KEY = "pacing_";


    public static AdPacingManager getInstance() {
        if (sInstance == null) {
            sInstance = new AdPacingManager();
        }
        return sInstance;
    }

    public void savePlacementShowTime(String placementId) {
        try {
            SPUtil.putLong(SDKContext.getInstance().getContext(), Const.SPU_NAME, PACING_KEY + placementId, System.currentTimeMillis());
        } catch (Exception e) {
            if (Const.DEBUG) {
                e.printStackTrace();
            }
        }
    }


    public boolean isPlacementInPacing(String placementId, PlaceStrategy placeStrategy) {
        if (placeStrategy == null) {
            return true;
        }
        if (placeStrategy.getUnitPacing() == -1) {
            return false;
        }
        long showTime = SPUtil.getLong(SDKContext.getInstance().getContext(), Const.SPU_NAME, PACING_KEY + placementId, 0L);
        if (System.currentTimeMillis() - showTime < 0) {
            savePlacementShowTime(placementId);
            //odd case, return false
            return false;
        } else if (System.currentTimeMillis() - showTime < placeStrategy.getUnitPacing()) {
            //in pacing
            return true;
        } else {
            //no in pacing
            return false;
        }
    }

    public void saveUnitGropuShowTime(final String placementId, final String unitGroupId) {
        TaskManager.getInstance().run_proxy(new Runnable() {
            @Override
            public void run() {
                try {
                    SPUtil.putLong(SDKContext.getInstance().getContext(), Const.SPU_NAME
                            , PACING_KEY + placementId + "_" + unitGroupId, System.currentTimeMillis());
                } catch (Exception e) {
                    if (Const.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public boolean isUnitGroupInPacing(String placementId, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        if (unitGroupInfo == null) {
            return true;
        }
        if (unitGroupInfo.getUnitPacing() == -1) {
            return false;
        }
        long showTime = SPUtil.getLong(SDKContext.getInstance().getContext(), Const.SPU_NAME
                , PACING_KEY + placementId + "_" + unitGroupInfo.unitId, 0L);
        if (System.currentTimeMillis() - showTime < 0) {
            saveUnitGropuShowTime(placementId, unitGroupInfo.unitId);
            //odd case, return false
            return false;
        } else if (System.currentTimeMillis() - showTime < unitGroupInfo.getUnitPacing()) {
            //in pacing
            return true;
        } else {
            //no in pacing
            return false;
        }

    }
}
