/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

/**
 * Online Api Ad Info
 */
public class OnlineApiOffer extends OwnBaseAdContent {

    int networkFirmId; //For online Api
    /**
     * Android Deeplink Target：
     * 0 – No limit (Default)
     * 1 – Target to non-install app
     * 2 – Target to installed app
     */
    public static final int DEEPLINK_TARGET_NON_INSTALL_APP = 1;
    public static final int DEEPLINK_TARGET_INSTALLED_APP = 2;
    int deeplinkTarget;

    long expireTime;
    long updateTime;

    public OnlineApiOffer() {

    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getNetworkFirmId() {
        return networkFirmId;
    }

    public void setNetworkFirmId(int networkFirmId) {
        this.networkFirmId = networkFirmId;
    }

    public int getDeeplinkTarget() {
        return deeplinkTarget;
    }

    public void setDeeplinkTarget(int deeplinkTarget) {
        this.deeplinkTarget = deeplinkTarget;
    }

    @Override
    public int getOfferSourceType() {
        return ONLINEAPI_TYPE;
    }

    public boolean isExpire() {
        if (System.currentTimeMillis() - updateTime > expireTime) {
            return true;
        }
        return false;
    }
}
