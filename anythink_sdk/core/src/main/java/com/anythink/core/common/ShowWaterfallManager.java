package com.anythink.core.common;

import android.text.TextUtils;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.strategy.PlaceStrategy;
import com.anythink.core.strategy.PlaceStrategyManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShowWaterfallManager {

    private static ShowWaterfallManager sIntance;
    private ConcurrentHashMap<String, PlacementWaterFall> mWaterMap;


    private ShowWaterfallManager() {
        mWaterMap = new ConcurrentHashMap<>();
    }

    public synchronized static ShowWaterfallManager getInstance() {
        if (sIntance == null) {
            sIntance = new ShowWaterfallManager();
        }
        return sIntance;
    }

    /**
     * Add the placement's Newest strategy
     *
     * @param placementId
     * @param requestId
     * @param placeStrategy
     */
    public synchronized void refreshPlacementWaterFall(String placementId, String requestId, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> waterFallList) {
        PlacementWaterFall waterFallItem = mWaterMap.get(placementId);
        if (waterFallItem == null) {
            waterFallItem = new PlacementWaterFall();
        }

        waterFallItem.addRequestWaterFall(requestId, placeStrategy, waterFallList);
        waterFallItem.lastRequestId = requestId;
        mWaterMap.put(placementId, waterFallItem);

        //Clean the old and finish waterfall
        waterFallItem.cleanOldRequestIdWaterfall(requestId);
    }


    /**
     * Add Adsource to WaterFall
     *
     * @param placementId
     * @param requestId
     * @param unitGroupInfos
     */
    public synchronized void addAdSourceToWaterFall(String placementId, final String requestId, final List<PlaceStrategy.UnitGroupInfo> unitGroupInfos) {
        //Add Adsource to current RequestId waterfall
        final PlacementWaterFall waterFallItem = mWaterMap.get(placementId);
        if (waterFallItem == null) {
            return;
        }

        for (PlaceStrategy.UnitGroupInfo item : unitGroupInfos) {
            waterFallItem.addAdSource(requestId, item);
        }

    }

    /**
     * Notfiy Finish
     *
     * @param placementId
     * @param requestId
     */
    public synchronized void finishFinalWaterFall(String placementId, String requestId) {
        PlacementWaterFall waterFallItem = mWaterMap.get(placementId);
        if (waterFallItem == null) {
            return;
        }
        waterFallItem.finishWaterFall(requestId);
    }


    /**
     * Use for isReady's Waterfall List
     *
     * @param placementId
     * @return
     */
    public List<PlaceStrategy.UnitGroupInfo> getNewestWaterFallForPlacementId(String placementId) {
        PlacementWaterFall waterFallItem = mWaterMap.get(placementId);
        if (waterFallItem == null || waterFallItem.getRequestWaterFall(waterFallItem.lastRequestId) == null) {
            PlaceStrategy placeStrategy = PlaceStrategyManager.getInstance(SDKContext.getInstance().getContext()).getPlaceStrategyByAppIdAndPlaceId(placementId);
            if (placeStrategy != null) {
                return placeStrategy.getNormalUnitGroupList();
            }
            return null;
        }

        List<PlaceStrategy.UnitGroupInfo> newestWaterFallList = waterFallItem.getRequestWaterFall(waterFallItem.lastRequestId);

        List<PlaceStrategy.UnitGroupInfo> unitGroupInfos = new ArrayList<>();
        unitGroupInfos.addAll(newestWaterFallList);
        return unitGroupInfos;
    }

    public String getWaterFallNewestRequestId(String placementId) {
        PlacementWaterFall waterFallItem = mWaterMap.get(placementId);
        if (waterFallItem != null) {
            return waterFallItem.lastRequestId;
        }
        return "";
    }

    /**
     * Record Placement Request WaterFall
     */
    class PlacementWaterFall {
        String lastRequestId;
        ConcurrentHashMap<String, RequestIdWaterFall> strategyForRequestIdMap = new ConcurrentHashMap<>();

        /**
         * Add RequestId Waterfall
         *
         * @param requestId
         * @param placeStrategy
         * @param list
         */
        private void addRequestWaterFall(String requestId, PlaceStrategy placeStrategy, List<PlaceStrategy.UnitGroupInfo> list) {
            RequestIdWaterFall requestIdWaterFall = new RequestIdWaterFall();
            requestIdWaterFall.placeStrategy = placeStrategy;

            CopyOnWriteArrayList<PlaceStrategy.UnitGroupInfo> waterFallList = new CopyOnWriteArrayList<>();
            waterFallList.addAll(list);
            requestIdWaterFall.waterfallList = waterFallList;

            strategyForRequestIdMap.put(requestId, requestIdWaterFall);
        }

        /**
         * Get Waterfall By RequestId
         *
         * @param requestId
         * @return
         */
        private List<PlaceStrategy.UnitGroupInfo> getRequestWaterFall(String requestId) {
            RequestIdWaterFall requestIdWaterFall = strategyForRequestIdMap.get(requestId);
            if (requestIdWaterFall != null) {
                return requestIdWaterFall.getWaterfallList();
            }
            return null;
        }

        /**
         * Add Adsource to WaterFall by RequestId
         */
        private void addAdSource(String requestId, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
            RequestIdWaterFall requestIdWaterFall = strategyForRequestIdMap.get(requestId);
            if (requestIdWaterFall != null) {
                requestIdWaterFall.addAdsource(unitGroupInfo);
            }
        }

        /**
         * Notify Request WaterFall Finish
         *
         * @param requestId
         */
        private void finishWaterFall(String requestId) {
            RequestIdWaterFall requestIdWaterFall = strategyForRequestIdMap.get(requestId);
            if (requestIdWaterFall != null) {
                requestIdWaterFall.notifyFinish();
            }
        }

        /**
         * Clean
         */
        private synchronized void cleanOldRequestIdWaterfall(String lastRequestId) {
            Iterator iter = strategyForRequestIdMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                if (key != null) {
                    String currentKey = key.toString();
                    RequestIdWaterFall requestIdWaterFall = strategyForRequestIdMap.get(currentKey);
                    if (requestIdWaterFall.isFinish() && !TextUtils.equals(lastRequestId, currentKey)) {
                        iter.remove();
                    }
                }
            }
        }

        /**
         * RequestId WaterFall
         */
        class RequestIdWaterFall {
            PlaceStrategy placeStrategy;
            CopyOnWriteArrayList<PlaceStrategy.UnitGroupInfo> waterfallList;
            boolean isFinish;

            private List<PlaceStrategy.UnitGroupInfo> getWaterfallList() {
                return waterfallList;
            }

            private synchronized void addAdsource(PlaceStrategy.UnitGroupInfo unitGroupInfo) {
                if (waterfallList == null) {
                    return;
                }

                if (unitGroupInfo.level == -1 || unitGroupInfo.ecpm == 0 || waterfallList.size() == 0) {
                    waterfallList.add(unitGroupInfo);
                } else {
                    for (int i = 0; i < waterfallList.size(); i++) {
                        PlaceStrategy.UnitGroupInfo item = waterfallList.get(i);
                        if (item.level == -1 || unitGroupInfo.ecpm >= item.ecpm) {
                            waterfallList.add(i, unitGroupInfo);
                            break;
                        } else {
                            if (i == waterfallList.size() - 1) {
                                waterfallList.add(unitGroupInfo);
                                break;
                            }
                        }
                    }
                }


            }

            private synchronized void notifyFinish() {
                if (isFinish) {
                    return;
                }
                isFinish = true;
            }

            private boolean isFinish() {
                return isFinish;
            }
        }
    }
}
