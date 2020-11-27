/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */
package com.anythink.core.hb.callback;

import com.anythink.core.strategy.PlaceStrategy;

import java.util.List;

public interface BiddingCallback {

    void onBiddingSuccess(List<PlaceStrategy.UnitGroupInfo> successList);

    void onBiddingFailed(List<PlaceStrategy.UnitGroupInfo> failedList);

    void onBiddingFinished();

}