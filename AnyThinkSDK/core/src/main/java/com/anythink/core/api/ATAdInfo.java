package com.anythink.core.api;

import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.entity.AdTrackingInfo;

/**
 * Mediation Info
 */
public class ATAdInfo {

    private int mNetworkType;
    private String mAdsourceId;
    private int mLevel;
    private double mEcpm;
    private boolean mIsHBAdsource;

    public ATAdInfo() {
        this.mNetworkType = -1;
        this.mAdsourceId = "";
        this.mLevel = -1;
        this.mEcpm = 0;
        this.mIsHBAdsource = false;
    }

    public void setNetworkType(int mNetworkType) {
        this.mNetworkType = mNetworkType;
    }

    public void setAdsourceId(String mAdsourceId) {
        this.mAdsourceId = mAdsourceId;
    }

    public int getNetworkType() {
        return mNetworkType;
    }

    public String getAdsourceId() {
        return mAdsourceId;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int mLevel) {
        this.mLevel = mLevel;
    }

    public double getEcpm() {
        return mEcpm;
    }

    public void setEcpm(double mEcpm) {
        this.mEcpm = mEcpm;
    }

    public boolean isHBAdsource() {
        return mIsHBAdsource;
    }

    public void setIsHBAdsource(boolean isHBAdsource) {
        this.mIsHBAdsource = isHBAdsource;
    }

    public static ATAdInfo fromAdapter(AnyThinkBaseAdapter adapter) {
        if (adapter != null) {
            return fromAdTrackingInfo(adapter.getTrackingInfo());
        }
        return new ATAdInfo();
    }

    public static ATAdInfo fromAdTrackingInfo(AdTrackingInfo trackingInfo) {
        ATAdInfo entity = new ATAdInfo();
        if (trackingInfo != null) {
            return fillData(entity, trackingInfo);
        }
        return entity;
    }

    private static ATAdInfo fillData(ATAdInfo entity, AdTrackingInfo trackingInfo) {
        entity.setNetworkType(trackingInfo.getmNetworkType());          // Mediation type
        entity.setAdsourceId(trackingInfo.getmUnitGroupUnitId());       // Adsource id
        entity.setEcpm(trackingInfo.getmBidPrice());                    //Adsource ecpm
        entity.setIsHBAdsource(trackingInfo.getmBidType() == 1);        //AdSource type, ture: headbidding, false: non-headbidding
        entity.setLevel(trackingInfo.getmLevel());                      //AdSource level

        return entity;
    }

    public String printInfo() {
        return "NetworkInfo -->" + "\n" +
                "\t" + "NetworkType: " + mNetworkType + "\n" +
                "\t" + "AdsourceId: " + mAdsourceId + "\n" +
                "\t" + "Level: " + mLevel + "\n" +
                "\t" + "ECPM: " + mEcpm + "\n" +
                "\t" + "IsHeadBiddingAdSource: " + mIsHBAdsource + "\n" +
                "<-- NetworkInfo";
    }
}
