/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.entity;

public class OfferClickResult {
    public String originUrl;
    public String resultUrl;
    public String clickId;

    public OfferClickResult(String originUrl, String resultUrl, String clickId) {
        this.originUrl = originUrl;
        this.resultUrl = resultUrl;
        this.clickId = clickId;
    }
}
