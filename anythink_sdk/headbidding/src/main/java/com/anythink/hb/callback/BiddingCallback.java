/*
 * Copyright (C) 2019 Mintegral, Inc. All rights reserved.
 */
package com.anythink.hb.callback;

import com.anythink.hb.data.BiddingResponse;

/**
 * Callback from bidder to transaction
 */
public interface BiddingCallback {

    void onBiddingResponse(BiddingResponse object);
}
