/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb;

import android.text.TextUtils;

import com.anythink.core.common.entity.ATHeadBiddingRequest;
import com.anythink.core.common.entity.BaseBiddingResult;
import com.anythink.core.hb.callback.BiddingCallback;
import com.anythink.core.strategy.PlaceStrategy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public abstract class BaseHeadBiddingHandler {

    protected ATHeadBiddingRequest mRequest;
    protected boolean mIsTestMode;

    public BaseHeadBiddingHandler(ATHeadBiddingRequest request) {
        this.mRequest = request;
    }

    protected void setTestMode(boolean isTest) {
        this.mIsTestMode = isTest;
    }

    protected abstract void startBidRequest(BiddingCallback biddingCallback);

    protected abstract void processUnitGrouInfo(PlaceStrategy.UnitGroupInfo unitGroupInfo, BaseBiddingResult biddingResult, long bidUseTime);

    protected abstract void onTimeout();

    protected JSONArray parseHBLogJSONArray(List<PlaceStrategy.UnitGroupInfo> unitGroupInfos) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (PlaceStrategy.UnitGroupInfo unitGroupInfo : unitGroupInfos) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("network_firm_id", unitGroupInfo.networkType);
                itemObject.put("ad_source_id", unitGroupInfo.unitId);
                itemObject.put("content", unitGroupInfo.content);

                if (unitGroupInfo.ecpm != 0) {
                    itemObject.put("price", unitGroupInfo.ecpm);
                }
                if (!TextUtils.isEmpty(unitGroupInfo.errorMsg)) {
                    itemObject.put("error", unitGroupInfo.errorMsg);
                }
                jsonArray.put(itemObject);
            }
        } catch (Exception e) {

        }

        return jsonArray;
    }

}
