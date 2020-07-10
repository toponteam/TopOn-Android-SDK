/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.callback;

import com.anythink.hb.data.AuctionResult;

/**
 * Callback from transaction to developer
 */
public interface BidRequestCallback {

    void onBidRequestCallback(String unitId, AuctionResult auctionResult);
}
