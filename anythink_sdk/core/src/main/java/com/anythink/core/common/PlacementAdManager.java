/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlacementAdManager {

    private static PlacementAdManager sInstance;

    private ConcurrentHashMap<String, CommonAdManager> sAdMangerMap;
    private ConcurrentHashMap<String, Map<String, Object>> mPlacementLocalSettingMap;

    private PlacementAdManager() {
        sAdMangerMap = new ConcurrentHashMap<>();
        mPlacementLocalSettingMap = new ConcurrentHashMap<>(3);
    }

    public synchronized static PlacementAdManager getInstance() {
        if (sInstance == null) {
            sInstance = new PlacementAdManager();
        }
        return sInstance;
    }

    public CommonAdManager getAdManager(String placementId) {
        return sAdMangerMap.get(placementId);
    }

    public void addAdManager(String placementId, CommonAdManager adManager) {
        sAdMangerMap.put(placementId, adManager);
    }

    public synchronized void putPlacementLocalSettingMap(String placementId, Map<String, Object> settingMap) {
        if (settingMap == null) {
            return;
        }
        mPlacementLocalSettingMap.put(placementId, settingMap);
    }

    public synchronized Map<String, Object> getPlacementLocalSettingMap(String placementId) {
        Map<String, Object> placementLocalMap = new HashMap<>(2);
        if (mPlacementLocalSettingMap != null) {
            Map<String, Object> saveMap = mPlacementLocalSettingMap.get(placementId);
            if (saveMap != null) {
                placementLocalMap.putAll(saveMap);
            }
        }
        return placementLocalMap;
    }

    public synchronized void addExtraInfoToLocalMap(String placementId, String key, Object object) {
        if (mPlacementLocalSettingMap == null) {
            mPlacementLocalSettingMap = new ConcurrentHashMap<>(6);
        }
        Map<String, Object> saveMap = mPlacementLocalSettingMap.get(placementId);
        if (saveMap == null) {
            saveMap = new HashMap<>(2);
            mPlacementLocalSettingMap.put(placementId, saveMap);
        }
        saveMap.put(key, object);
    }
}
