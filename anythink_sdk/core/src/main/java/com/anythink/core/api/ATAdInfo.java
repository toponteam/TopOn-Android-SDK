/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

import android.text.TextUtils;

import com.anythink.core.common.MyOfferAPIProxy;
import com.anythink.core.common.base.AnyThinkBaseAdapter;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.AdTrackingInfo;
import com.anythink.core.common.utils.CommonSDKUtil;

import org.json.JSONObject;

import java.util.Map;

/**
 * Mediation Info
 */
public class ATAdInfo {

    private ATBaseAdAdapter mBaseAdapter;

    private int mNetworkFirmId;
    private String mAdsourceId;
    private int mAdsourceIndex;
    private double mEcpm;
    private int mIsHBAdsource;

    private String mShowId;
    private Double mPublisherRevenue;
    private String mCurrency;
    private String mCountry;
    private String mTopOnPlacementId;

    private String mTopOnAdFormat;
    private String mEcpmPrecision;
    private String mAdNetworkType;
    private String mNetworkPlacementId;
    private int mEcpmLevel;

    private int mSegmentId;
    private String mScenarioId;
    private String mScenarioRewardName;
    private int mScenarioRewardNumber;

    private String mSubChannel;
    private String mChannel;
    private Map<String, Object> mCustomRule;

    public ATAdInfo() {
        this.mNetworkFirmId = -1;
        this.mAdsourceId = "";
        this.mAdsourceIndex = -1;
        this.mEcpm = 0;
        this.mIsHBAdsource = 0;

        this.mShowId = "";
        this.mPublisherRevenue = 0d;
        this.mCurrency = "";
        this.mCountry = "";
        this.mTopOnPlacementId = "";

        this.mTopOnAdFormat = "";
        this.mEcpmPrecision = "publisher_defined";
        this.mAdNetworkType = "Network";
        this.mNetworkPlacementId = "";
        this.mEcpmLevel = 1;

        this.mSegmentId = 0;
        this.mScenarioId = "";
        this.mScenarioRewardName = "";
        this.mScenarioRewardNumber = 0;

        this.mSubChannel = "";
        this.mChannel = "";
        this.mCustomRule = null;
    }


    /**
     * Use {@link #getNetworkFirmId()} method instead of this method. This method works temporarily and will be removed in the future
     */
    @Deprecated
    public int getNetworkType() {
        return mNetworkFirmId;
    }

    public int getNetworkFirmId() {
        return mNetworkFirmId;
    }

    public String getAdsourceId() {
        return mAdsourceId;
    }

    /**
     * Use {@link #getAdsourceIndex()} ()} method instead of this method. This method works temporarily and will be removed in the future
     */
    @Deprecated
    public int getLevel() {
        return mAdsourceIndex;
    }

    public int getAdsourceIndex() {
        return mAdsourceIndex;
    }


    public double getEcpm() {
        return mEcpm;
    }

    /**
     * Use {@link #isHeaderBiddingAdsource()} ()} method instead of this method. This method works temporarily and will be removed in the future
     */
    @Deprecated
    public boolean isHBAdsource() {
        return mIsHBAdsource == 1;
    }

    /**
     * @return 1：HeaderBidding<p>
     * 0：No HeaderBidding
     */
    public int isHeaderBiddingAdsource() {
        return mIsHBAdsource;
    }

    public String getShowId() {
        return mShowId;
    }

    public Double getPublisherRevenue() {
        return mPublisherRevenue;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getTopOnPlacementId() {
        return mTopOnPlacementId;
    }

    public String getTopOnAdFormat() {
        return mTopOnAdFormat;
    }

    public String getEcpmPrecision() {
        return mEcpmPrecision;
    }

    public String getAdNetworkType() {
        return mAdNetworkType;
    }

    public String getNetworkPlacementId() {
        return mNetworkPlacementId;
    }

    public int getEcpmLevel() {
        return mEcpmLevel;
    }

    public int getSegmentId() {
        return mSegmentId;
    }

    public String getScenarioId() {
        return mScenarioId;
    }

    /**
     * Match the scenario id to return the name of the scene reward information,<p>
     * if it does not match, return the name of the default scene reward information
     */
    public String getScenarioRewardName() {
        return mScenarioRewardName;
    }

    /**
     * Match the scenario id to return the number of the scene reward information,<p>
     * if it does not match, return the number of the default scene reward information
     */
    public int getScenarioRewardNumber() {
        return mScenarioRewardNumber;
    }

    /**
     * Use {@link #getScenarioRewardName()} ()} method instead of this method. This method returns an empty string and will be removed in the future
     */
    @Deprecated
    public String getPlacementRewardName() {
        return "";
    }

    /**
     * Use {@link #getScenarioRewardNumber()} ()} method instead of this method. This method returns 0 and will be removed in the future
     */
    @Deprecated
    public int getPlacementRewardNumber() {
        return 0;
    }

    public String getSubChannel() {
        return mSubChannel;
    }

    public String getChannel() {
        return mChannel;
    }

    public String getCustomRule() {
        if (mCustomRule != null) {
            return new JSONObject(mCustomRule).toString();
        } else {
            return "";
        }
    }

    public static ATAdInfo fromAdapter(AnyThinkBaseAdapter adapter) {
        if (adapter != null) {
            ATAdInfo atAdInfo = fromAdTrackingInfo(adapter.getTrackingInfo());
            if (adapter instanceof ATBaseAdAdapter) {
                atAdInfo.mBaseAdapter = (ATBaseAdAdapter) adapter;
            }
            return atAdInfo;
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
        entity.mNetworkFirmId = trackingInfo.getmNetworkType();          // Mediation type
        entity.mAdsourceId = trackingInfo.getmUnitGroupUnitId();       // Adsource id
        entity.mAdsourceIndex = trackingInfo.getImpressionLevel();              //AdSource impression Level
        entity.mEcpm = trackingInfo.getmBidPrice();                    //Adsource ecpm
        entity.mIsHBAdsource = trackingInfo.getmBidType();        //AdSource type, 1: headbidding, 0: non-headbidding


        entity.mShowId = trackingInfo.getmShowId();
        entity.mPublisherRevenue = entity.mEcpm / 1000;
        entity.mCurrency = trackingInfo.getmCurrency();
        entity.mCountry = trackingInfo.getmCountry();

        entity.mTopOnAdFormat = CommonSDKUtil.getFormatString(trackingInfo.getmAdType());
        entity.mTopOnPlacementId = trackingInfo.getmPlacementId();

        // publisher_defined, estimated, exact
        if (entity.mIsHBAdsource == 1) {
            entity.mEcpmPrecision = "exact";
        } else if (!TextUtils.isEmpty(trackingInfo.getmEcpmPrecision())) {
            entity.mEcpmPrecision = trackingInfo.getmEcpmPrecision();
        }

        // Network, Cross_Promotion, Adx
        if (trackingInfo.getmNetworkType() == MyOfferAPIProxy.MYOFFER_NETWORK_FIRM_ID) {
            entity.mAdNetworkType = "Cross_Promotion";
        } else {
            entity.mAdNetworkType = "Network";
        }

        entity.mNetworkPlacementId = trackingInfo.getmNetworkPlacementId();
        entity.mEcpmLevel = trackingInfo.getmEcpmLevel();
        entity.mSegmentId = trackingInfo.getmGroupId();
        entity.mScenarioId = trackingInfo.getmScenario();// RewardVideo & Interstitial

        //For RewardVideo
        if (TextUtils.equals(Const.FORMAT_STRING.REWARDEDVIDEO, entity.mTopOnAdFormat)) {
            Map<String, ATRewardInfo> scenarioRewardMap = trackingInfo.getmScenarioRewardMap();
            if (scenarioRewardMap != null && scenarioRewardMap.containsKey(entity.mScenarioId)) {
                ATRewardInfo atRewardInfo = scenarioRewardMap.get(entity.mScenarioId);
                if (atRewardInfo != null) {
                    entity.mScenarioRewardName = atRewardInfo.rewardName;
                    entity.mScenarioRewardNumber = atRewardInfo.rewardNumber;
                }
            }

            if (TextUtils.isEmpty(entity.mScenarioRewardName) || entity.mScenarioRewardNumber == 0) {
                ATRewardInfo atRewardInfo = trackingInfo.getmPlacementRewardInfo();
                if (atRewardInfo != null) {
                    entity.mScenarioRewardName = atRewardInfo.rewardName;
                    entity.mScenarioRewardNumber = atRewardInfo.rewardNumber;
                }
            }
        }
        //For RewardVideo

        entity.mChannel = SDKContext.getInstance().getChannel();
        entity.mSubChannel = SDKContext.getInstance().getSubChannel();
        entity.mCustomRule = trackingInfo.getmCustomRule();

        return entity;
    }

    /**
     * Use {@link #toString()} method instead of this method. This method returns an empty string and will be removed in the future
     */
    @Deprecated
    public String printInfo() {
        return "";
    }

    @Override
    public String toString() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", mShowId);
            jsonObject.put("publisher_revenue", mPublisherRevenue);
            jsonObject.put("currency", mCurrency);
            jsonObject.put("country", mCountry);
            jsonObject.put("adunit_id", mTopOnPlacementId);

            jsonObject.put("adunit_format", mTopOnAdFormat);
            jsonObject.put("precision", mEcpmPrecision);
            jsonObject.put("network_type", mAdNetworkType);
            jsonObject.put("network_placement_id", mNetworkPlacementId);
            jsonObject.put("ecpm_level", mEcpmLevel);

            jsonObject.put("segment_id", mSegmentId);
            if (!TextUtils.isEmpty(mScenarioId)) {
                jsonObject.put("scenario_id", mScenarioId);
            }

            if (!TextUtils.isEmpty(mScenarioRewardName) && mScenarioRewardNumber != 0) {
                jsonObject.put("scenario_reward_name", mScenarioRewardName);
                jsonObject.put("scenario_reward_number", mScenarioRewardNumber);
            }

            if (!TextUtils.isEmpty(mChannel)) {
                jsonObject.put("channel", mChannel);
            }
            if (!TextUtils.isEmpty(mSubChannel)) {
                jsonObject.put("sub_channel", mSubChannel);
            }
            if (mCustomRule != null && mCustomRule.size() > 0) {
                jsonObject.put("custom_rule", new JSONObject(mCustomRule));
            }
            jsonObject.put("network_firm_id", mNetworkFirmId);

            jsonObject.put("adsource_id", mAdsourceId);
            jsonObject.put("adsource_index", mAdsourceIndex);
            jsonObject.put("adsource_price", mEcpm);
            jsonObject.put("adsource_isheaderbidding", mIsHBAdsource);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

}
