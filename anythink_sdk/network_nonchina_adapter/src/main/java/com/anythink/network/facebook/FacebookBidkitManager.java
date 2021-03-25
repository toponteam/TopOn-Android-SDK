/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.network.facebook;

import android.content.Context;

import com.anythink.core.api.MediationBidManager;
import com.anythink.core.strategy.PlaceStrategy;
import com.facebook.biddingkit.bridge.BiddingKit;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FacebookBidkitManager extends MediationBidManager {
    private static FacebookBidkitManager sInstance;

    boolean isInit;
    ConcurrentHashMap<String, FacebookBidkitAuction> placementWaterFall;

    private FacebookBidkitManager() {
        placementWaterFall = new ConcurrentHashMap<>();
    }

    public synchronized static FacebookBidkitManager getInstance() {
        if (sInstance == null) {
            sInstance = new FacebookBidkitManager();
        }
        return sInstance;
    }

    @Override
    public void startBid(Context context, int format, final String placementId
            , List<PlaceStrategy.UnitGroupInfo> bidUnitGroupInfos, List<PlaceStrategy.UnitGroupInfo> normalUnitGroupInofs
            , final BidListener bidListener, long timeOut) {
        //TODO synchronized
        try {
            if (!isInit) {
                JSONObject jsonObject = new JSONObject();

                JSONObject timeoutObject = new JSONObject();
                try {
                    timeoutObject.put("timeout_ms", timeOut);
                    jsonObject.put("auction", timeoutObject);
                } catch (Throwable e) {

                }
                BiddingKit.init(context.getApplicationContext(), jsonObject.toString());
                isInit = true;
            }

            final FacebookBidkitAuction bidkitAuction = new FacebookBidkitAuction(context, format, bidUnitGroupInfos, normalUnitGroupInofs);
            bidkitAuction.startBidding(mRequestUrl, new BidListener() {
                @Override
                public void onBidSuccess(List<PlaceStrategy.UnitGroupInfo> bidUnitGroupInfos) {
                    placementWaterFall.put(placementId, bidkitAuction);
                    if (bidListener != null) {
                        bidListener.onBidSuccess(bidUnitGroupInfos);
                    }
                }

                @Override
                public void onBidFail(String errorMsg) {
                    //Nothing to do
                }
            });
        } catch (Throwable e) {
            if (bidListener != null) {
                bidListener.onBidFail(e.getMessage());
            }
        }


    }

    @Override
    public void notifyWinnerDisplay(String placementId, PlaceStrategy.UnitGroupInfo unitGroupInfo) {
        try {
            FacebookBidkitAuction facebookBidkitAuction = placementWaterFall.get(placementId);
            if (facebookBidkitAuction != null) {
                facebookBidkitAuction.notifyWinnerDisplay(unitGroupInfo);
            }
        } catch (Throwable e) {

        }
    }

}
