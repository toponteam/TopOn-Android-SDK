/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import com.anythink.core.api.ATBaseAdAdapter;
import com.anythink.core.api.BaseAd;
import com.anythink.core.common.base.SDKContext;

import java.util.ArrayList;
import java.util.List;

public class UnitgroupCacheInfo {
    public int requestLevel;
    public String requestId;
    private List<AdCacheInfo> adCacheInfoList;

    public synchronized void setAdCacheInfoList(List<AdCacheInfo> adCacheInfoList) {
        synchronized (this) {
            this.adCacheInfoList = adCacheInfoList;
        }
    }

    public synchronized AdCacheInfo getAdCacheInfo() {
        if (adCacheInfoList != null) {
            for (AdCacheInfo adCacheInfo : adCacheInfoList) {
                if (adCacheInfo.getShowTime() <= 0) {
                    //Mark the last offer
                    adCacheInfo.setLast(adCacheInfoList.indexOf(adCacheInfo) >= adCacheInfoList.size() - 1);
                    return adCacheInfo;
                }
            }
        }
        return null;
    }


    /**
     * Clear all AdCache
     **/
    public synchronized void destoryCache() {
        if (adCacheInfoList != null) {
            adCacheInfoList.clear();
            adCacheInfoList = null;
        }
    }

    /**
     * Remove single cache
     *
     * @param removeCacheInfo
     */
    public synchronized void removeAdCache(AdCacheInfo removeCacheInfo) {
        if (adCacheInfoList != null && adCacheInfoList.size() > 0) {
            adCacheInfoList.remove(removeCacheInfo);
        }
    }


    /**
     * Refresh Offer TrackingInfo
     *
     * @param adTrackingInfo
     */
    public void refreshCacheInfo(AdTrackingInfo adTrackingInfo, int requestLevel) {
        synchronized (this) {
            List<AdCacheInfo> tempList = new ArrayList<>();
            this.requestLevel = requestLevel;
            this.requestId = adTrackingInfo.getmRequestId();
            if (adCacheInfoList != null) {

                for (AdCacheInfo adCacheInfo : adCacheInfoList) {

                    if (adCacheInfo.isUpStatusAvaiable() && adCacheInfo.getUpdateTime() + adCacheInfo.getCacheTime() > System.currentTimeMillis() && adCacheInfo.isNetworkAdReady()) {
                        //Only refresh available up_status and offer doesn't out of date
                        ATBaseAdAdapter baseAdapter = adCacheInfo.getBaseAdapter();
                        //Create TrackingInfo
                        baseAdapter.setTrackingInfo(adTrackingInfo); //Refresh TrackingInfo in Adapter
                        //NativeAd refresh Ad Tracking Info
                        BaseAd baseAd = adCacheInfo.getAdObject();
                        if (baseAd != null) {
                            baseAd.setTrackingInfo(adTrackingInfo);
                        }
                        //set placement id for network
                        adTrackingInfo.setmNetworkPlacementId(baseAdapter.getNetworkPlacementId());
                        adCacheInfo.setRequestLevel(requestLevel);
                        tempList.add(adCacheInfo);

                    }
                }
            }
            adCacheInfoList = tempList;
        }
    }

    public boolean isExistCache() {
        if (adCacheInfoList != null && adCacheInfoList.size() > 0) {
            return true;
        }
        return false;
    }

//    @Deprecated
//    private synchronized void cleanNoShowCache() {
//        if (adCacheInfoList != null) {
//            for (int i = adCacheInfoList.size() - 1; i >= 0; i--) {
//                AdCacheInfo adCacheInfo = adCacheInfoList.get(i);
//                final BaseAd baseAd = adCacheInfo.getAdObject();
//                final ATBaseAdAdapter baseAdapter = adCacheInfo.getBaseAdapter();
//                int showTime = adCacheInfo.getShowTime();
//
//                if (baseAd != null && showTime == 0) {
//                    SDKContext.getInstance().runOnMainThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            baseAd.destroy();
//                        }
//                    });
//                    adCacheInfoList.remove(i);
//                    continue;
//                }
//                if (baseAdapter != null && showTime == 0) {
//                    SDKContext.getInstance().runOnMainThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            baseAdapter.destory();
//                        }
//                    });
//                    adCacheInfoList.remove(i);
//                    continue;
//                }
//            }
//        }
//    }
}
