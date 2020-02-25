package com.anythink.core.common.entity;

import com.anythink.core.common.base.AnyThinkBaseAdapter;

public class AdCacheInfo {

    private int level; //AdSource level
    private long updateTime; //AdSource updateTime
    private AnyThinkBaseAdapter baseAdapter; //AdSource Adapter
    private BaseAd adObject; //Ad object(just use for NativeAd)
    private int showTime; //Show Time
    private boolean isLast; //Last offer?
    private long cacheTime; //Avail cache time

    private String originRequestId; //Origin requestId

    /**
     * Add by v5.1.0
     */
    private int upStatus;
    private long upStatusCacheTime;

    /**
     * Check upstatus available
     **/
    public boolean isUpStatusAvaiable() {
        if (upStatus == 1 && System.currentTimeMillis() - updateTime < upStatusCacheTime) {
            return true;
        }
        return false;
    }

    public void setUpStatusCacheTime(long upStatusCacheTime) {
        this.upStatusCacheTime = upStatusCacheTime;
    }

    public void setOriginRequestId(String originRequestId) {
        this.originRequestId = originRequestId;
    }

    public String getOriginRequestId() {
        return this.originRequestId;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public int getShowTime() {
        return showTime;
    }

    public void setShowTime(int showTime) {
        this.showTime = showTime;
        //Record impression，set upstatus=0
        if (showTime >= 1) {
            upStatus = 0;
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        upStatus = 1;
        this.updateTime = updateTime;
    }

    public AnyThinkBaseAdapter getBaseAdapter() {
        return baseAdapter;
    }

    public void setBaseAdapter(AnyThinkBaseAdapter baseAdapter) {
        this.baseAdapter = baseAdapter;
    }

    public BaseAd getAdObject() {
        return adObject;
    }

    public void setAdObject(BaseAd adObject) {
        this.adObject = adObject;
    }

    public boolean isNetworkAdReady() {
        try {
            if (baseAdapter != null && adObject != null) {
                return true;
            }

            if (baseAdapter != null) {
                return baseAdapter.isAdReady();
            }

        } catch (Exception e) {

        }
        return false;
    }
}
