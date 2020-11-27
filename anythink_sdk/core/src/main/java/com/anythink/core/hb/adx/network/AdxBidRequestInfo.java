/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb.adx.network;


import com.anythink.core.hb.adx.BidRequest;
import com.anythink.core.strategy.PlaceStrategy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class AdxBidRequestInfo extends BaseNetworkInfo {
    int bannerWidth = 0;
    int bannerHeight = 0;
    JSONArray excludePkgJSONArrray;
    String adSourceId;

    int networkFirmId;

    public AdxBidRequestInfo(String format, PlaceStrategy.UnitGroupInfo unitGroupInfo, List<String> excludeOfferPkgArray) {
        super(format);
        this.adSourceId = unitGroupInfo.unitId;
        this.networkFirmId = unitGroupInfo.networkType;
        try {
            if (excludeOfferPkgArray != null && excludeOfferPkgArray.size() > 0) {
                excludePkgJSONArrray = new JSONArray(excludeOfferPkgArray);
            }
        } catch (Exception e) {

        }

    }


    @Override
    public String getRequestInfoId() {
        return adSourceId;
    }

    @Override
    public String getSDKVersion() {
        return "";
    }

    @Override
    public JSONObject toRequestJSONObject() {
        JSONObject networkObject = new JSONObject();
        try {
            networkObject.put(BidRequest.UNIT_ID, adSourceId);
            networkObject.put(BidRequest.FORMAT, format);
            networkObject.put(BidRequest.NW_FIRM_ID, networkFirmId);
            if (excludePkgJSONArrray != null) {
                networkObject.put(BidRequest.EXCLUDE_OFFER, excludePkgJSONArrray);
            }
        } catch (Exception e) {

        }
        return networkObject;
    }

}
