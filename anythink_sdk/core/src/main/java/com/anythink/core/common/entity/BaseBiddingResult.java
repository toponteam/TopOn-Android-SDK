/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

public class BaseBiddingResult {

    public boolean isSuccess;
    public double price;
    public String token;//payload、bidId
    public String errorMsg;
    public String winNoticeUrl;
    public String loseNoticeUrl;

    public BaseBiddingResult(boolean isSuccess, double price, String token, String winNoticeUrl, String loseNoticeUrl, String errorMsg) {
        this.isSuccess = isSuccess;
        this.price = price;
        this.token = token;
        this.winNoticeUrl = winNoticeUrl;
        this.loseNoticeUrl = loseNoticeUrl;
        this.errorMsg = errorMsg;
    }

}
