/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import android.content.Context;

import com.anythink.core.strategy.PlaceStrategy;

import java.util.List;

public class ATHeadBiddingRequest {
    public Context context;
    public String requestId;
    public String placementId;
    public int format;
    public long hbBidMaxTimeOut;
    public List<PlaceStrategy.UnitGroupInfo> hbList;

    public long hbWaitingToReqeustTime;//c2s
    public String s2sBidUrl;//s2s

    private ATHeadBiddingRequest createRequest() {
        ATHeadBiddingRequest atHeadBiddingRequest = new ATHeadBiddingRequest();
        atHeadBiddingRequest.context = this.context;
        atHeadBiddingRequest.requestId = this.requestId;
        atHeadBiddingRequest.placementId = this.placementId;
        atHeadBiddingRequest.format = this.format;
        if (hbBidMaxTimeOut < 0) {
            atHeadBiddingRequest.hbBidMaxTimeOut = 10000;
        } else {
            atHeadBiddingRequest.hbBidMaxTimeOut = this.hbBidMaxTimeOut;
        }

        return atHeadBiddingRequest;
    }

    public ATHeadBiddingRequest createS2SRequest(List<PlaceStrategy.UnitGroupInfo> hbList) {
        ATHeadBiddingRequest atHeadBiddingRequest = this.createRequest();
        atHeadBiddingRequest.hbList = hbList;

        atHeadBiddingRequest.s2sBidUrl = this.s2sBidUrl;

        return atHeadBiddingRequest;
    }

    public ATHeadBiddingRequest createC2SRequest(List<PlaceStrategy.UnitGroupInfo> hbList) {
        ATHeadBiddingRequest atHeadBiddingRequest = this.createRequest();
        atHeadBiddingRequest.hbList = hbList;

        return atHeadBiddingRequest;
    }

}
