/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.hb;

import com.anythink.core.common.entity.ATHeadBiddingRequest;
import com.anythink.core.common.entity.BaseBiddingResult;
import com.anythink.core.hb.callback.BiddingCallback;
import com.anythink.core.strategy.PlaceStrategy;

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

}
