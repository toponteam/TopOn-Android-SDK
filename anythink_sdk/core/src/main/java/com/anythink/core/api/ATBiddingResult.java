/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

import com.anythink.core.common.entity.BaseBiddingResult;

public class ATBiddingResult extends BaseBiddingResult {

    private ATBiddingResult(boolean isSuccess, double price, String token, String winNoticeUrl, String loseNoticeUrl, String errorMsg) {
        super(isSuccess, price, token, winNoticeUrl, loseNoticeUrl, errorMsg);
    }

    public static ATBiddingResult success(double price, String token, String winNoticeUrl, String loseNoticeUrl) {
        return new ATBiddingResult(true, price, token, winNoticeUrl, loseNoticeUrl, null);
    }

    public static ATBiddingResult fail(String errorMsg) {
        return new ATBiddingResult(false, 0, null, null, null, errorMsg);
    }

}
