/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

public class AdxOffer extends OwnBaseAdContent {
    private String bidId;

    public AdxOffer() {

    }

    public String getBidId() {
        return bidId;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    @Override
    public int getOfferSourceType() {
        return ADX_TYPE;
    }

}
