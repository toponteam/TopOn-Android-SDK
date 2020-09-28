/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.callback;

import com.anythink.hb.data.AuctionResult;
import com.anythink.hb.data.BidRequestInfo;

/**
 * Callback from transaction to developer
 */
public interface BidRequestCallback {

    void onError(String placementId, BidRequestInfo bidRequestInfo, Throwable e);

    void onBidResultWhenWaitingTimeout(String placementId, AuctionResult auctionResult);

    void onBidEachResult(String placementId, AuctionResult auctionResult);

    void onBidRequestFinished(String placementId, AuctionResult auctionResult);
}
