/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

import android.content.Context;

import com.anythink.core.strategy.PlaceStrategy;

import java.util.List;

public abstract class MediationBidManager {
    protected String mRequestUrl;

    public abstract void startBid(Context context, int format, String placementId
            , List<PlaceStrategy.UnitGroupInfo> bidUnitGroupInfos
            , List<PlaceStrategy.UnitGroupInfo> normalUnitGroupInofs
            , BidListener bidListener
            , long timeOut);

    public abstract void notifyWinnerDisplay(String placementId, PlaceStrategy.UnitGroupInfo unitGroupInfo);

    public void setBidRequestUrl(String requestUrl) {
        mRequestUrl = requestUrl;
    }

    public interface BidListener {
        void onBidSuccess(List<PlaceStrategy.UnitGroupInfo> bidUnitGroupInfos);
        //No using
        void onBidFail(String errorMsg);
    }
}
