/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb.adx.network;


import android.text.TextUtils;

import com.anythink.core.common.base.Const;
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

        getDataFrom(unitGroupInfo);

        try {
            if (excludeOfferPkgArray != null && excludeOfferPkgArray.size() > 0) {
                excludePkgJSONArrray = new JSONArray(excludeOfferPkgArray);
            }
        } catch (Exception e) {

        }

    }

    private void getDataFrom(PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        if (TextUtils.equals(Const.FORMAT.BANNER_FORMAT, this.format)) {
            if (66 == networkFirmId) {//adx
                try {
                    JSONObject jsonObject = new JSONObject(unitGroupInfo.content);
                    String size = jsonObject.optString("size");
                    if (!TextUtils.isEmpty(size)) {
                        String[] sizes = size.split("x");
                        this.bannerWidth = Integer.parseInt(sizes[0]);
                        this.bannerHeight = Integer.parseInt(sizes[1]);
                    }
                } catch (Exception e) {
                    if (Const.DEBUG) {
                        e.printStackTrace();
                    }
                }
            }
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

            //for banner
            switch (this.format) {
                case Const.FORMAT.BANNER_FORMAT:
                    networkObject.put(BidRequest.AD_WIDTH, bannerWidth);
                    networkObject.put(BidRequest.AD_HEIGHT, bannerHeight);
                    break;

                case Const.FORMAT.SPLASH_FORMAT:
                    networkObject.put(BidRequest.GET_OFFER, 2);//2: receive offer data
                    break;
            }

            if (excludePkgJSONArrray != null) {
                networkObject.put(BidRequest.EXCLUDE_OFFER, excludePkgJSONArrray);
            }
        } catch (Exception e) {

        }
        return networkObject;
    }

}
