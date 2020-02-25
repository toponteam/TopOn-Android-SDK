package com.anythink.core.common.entity;

import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.strategy.PlaceStrategy;

import java.util.ArrayList;
import java.util.List;

public class UnitgroupCacheInfo {
    public int level;
    public String requestId;
    public PlaceStrategy placeStrategy;
    public List<AdCacheInfo> adCacheInfoList;

    public synchronized void addAdCacheInfo(AdCacheInfo adCacheInfo) {
        synchronized (this) {
            if (adCacheInfoList == null) {
                adCacheInfoList = new ArrayList<>();
            }
            adCacheInfoList.add(adCacheInfo);
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

    public synchronized AdCacheInfo getHasShowAdCacheInfo() {
        if (adCacheInfoList != null) {
            for (AdCacheInfo adCacheInfo : adCacheInfoList) {
                if (adCacheInfo.getShowTime() >= 1) {
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
        }
    }


    /**
     * Refresh Offer TrackingInfo
     *
     * @param adTrackingInfo
     * @param placeStrategy
     * @param unitGroupInfo
     */
    public void refreshCacheInfo(AdTrackingInfo adTrackingInfo, PlaceStrategy placeStrategy, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        synchronized (this) {
            List<AdCacheInfo> tempList = new ArrayList<>();
            this.level = unitGroupInfo.level;
            this.requestId = adTrackingInfo.getmRequestId();
            this.placeStrategy = placeStrategy;
            if (adCacheInfoList != null) {

                for (AdCacheInfo adCacheInfo : adCacheInfoList) {

                    if (adCacheInfo.isUpStatusAvaiable() && adCacheInfo.getUpdateTime() + adCacheInfo.getCacheTime() > System.currentTimeMillis() && adCacheInfo.isNetworkAdReady()) {
                        //Only refresh available up_status and offer doesn't out of date
                        AnyThinkBaseAdapter baseAdapter = adCacheInfo.getBaseAdapter();
                        //Create TrackingInfo
                        baseAdapter.setTrackingInfo(adTrackingInfo); //Refresh TrackingInfo in Adapter
                        adCacheInfo.setLevel(this.level);
                        tempList.add(adCacheInfo);
                    }
                }
            }
            adCacheInfoList = tempList;
        }
    }


    @Deprecated
    private synchronized void cleanNoShowCache() {
        if (adCacheInfoList != null) {
            for (int i = adCacheInfoList.size() - 1; i >= 0; i--) {
                AdCacheInfo adCacheInfo = adCacheInfoList.get(i);
                final BaseAd baseAd = adCacheInfo.getAdObject();
                final AnyThinkBaseAdapter baseAdapter = adCacheInfo.getBaseAdapter();
                int showTime = adCacheInfo.getShowTime();

                if (baseAd != null && showTime == 0) {
                    SDKContext.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            baseAd.destroy();
                        }
                    });
                    adCacheInfoList.remove(i);
                    continue;
                }
                if (baseAdapter != null && showTime == 0) {
                    SDKContext.getInstance().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            baseAdapter.clean();
                        }
                    });
                    adCacheInfoList.remove(i);
                    continue;
                }
            }
        }
    }
}
