/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Auction result for one runtime bidding
 */
public class AuctionResult {
    private String transactionId = "";
    private String unitId = "";
    private List<BiddingResponse> successBidders = new ArrayList<>();
    private List<BiddingResponse> failedBidders = new ArrayList<>();

    public String getTransactionId() {
        return transactionId;
    }

    public void setRequestId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setPlacementId(String unitId) {
        this.unitId = unitId;
    }


    public List<BiddingResponse> getSuccessBidders() {
        return successBidders;
    }

    public void setSuccessBidders(List<BiddingResponse> successBidders) {
        this.successBidders = successBidders;
    }

    public List<BiddingResponse> getFailedBidders() {
        return failedBidders;
    }

    public void setFailedBidders(List<BiddingResponse> failedBidders) {
        this.failedBidders = failedBidders;
    }

}
