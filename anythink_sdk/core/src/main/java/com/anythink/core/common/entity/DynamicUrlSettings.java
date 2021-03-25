/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

public class DynamicUrlSettings {
    private String adxRequestHttpUrl;
    private String adxBidRequestHttpUrl;
    private String adxTrackRequestHttpUrl;
    private String onlineApiRequestHttpUrl;

    public String getOnlineApiRequestHttpUrl() {
        return onlineApiRequestHttpUrl;
    }

    public void setOnlineApiRequestHttpUrl(String onlineApiRequestHttpUrl) {
        this.onlineApiRequestHttpUrl = onlineApiRequestHttpUrl;
    }

    public String getAdxRequestHttpUrl() {
        return adxRequestHttpUrl;
    }

    public void setAdxRequestHttpUrl(String adxRequestHttpUrl) {
        this.adxRequestHttpUrl = adxRequestHttpUrl;
    }

    public String getAdxBidRequestHttpUrl() {
        return adxBidRequestHttpUrl;
    }

    public void setAdxBidRequestHttpUrl(String adxBidRequestHttpUrl) {
        this.adxBidRequestHttpUrl = adxBidRequestHttpUrl;
    }

    public String getAdxTrackRequestHttpUrl() {
        return adxTrackRequestHttpUrl;
    }

    public void setAdxTrackRequestHttpUrl(String adxTrackRequestHttpUrl) {
        this.adxTrackRequestHttpUrl = adxTrackRequestHttpUrl;
    }
}
