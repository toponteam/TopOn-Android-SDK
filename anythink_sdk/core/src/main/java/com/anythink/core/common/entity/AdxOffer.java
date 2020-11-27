/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import android.text.TextUtils;


import com.anythink.core.common.base.Const;

import java.util.ArrayList;
import java.util.List;

public class AdxOffer extends BaseAdContent<AdxAdSetting> {
    private String bidId;
    AdxAdSetting adxAdSetting;
    AdxTrackObject adxTrackObject;

    int rating;
    long videoLength;
    String videoScreen;
    String endcardUrl;


    public AdxOffer() {

    }

    public String getBidId() {
        return bidId;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    public long getVideoLength() {
        return videoLength;
    }

    public void setVideoLength(long videoLength) {
        this.videoLength = videoLength;
    }

    public String getVideoScreen() {
        return videoScreen;
    }

    public void setVideoScreen(String videoScreen) {
        this.videoScreen = videoScreen;
    }

    public String getEndcardUrl() {
        return endcardUrl;
    }

    public void setEndcardUrl(String endcardUrl) {
        this.endcardUrl = endcardUrl;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public AdxAdSetting getAdxAdSetting() {
        return adxAdSetting;
    }

    public void setAdxAdSetting(AdxAdSetting adxAdSetting) {
        this.adxAdSetting = adxAdSetting;
    }

    public AdxTrackObject getAdxTrackObject() {
        return adxTrackObject;
    }

    public void setAdxTrackObject(AdxTrackObject adxTrackObject) {
        this.adxTrackObject = adxTrackObject;
    }

    @Override
    public List<String> getUrlList(AdxAdSetting baseAdSetting) {
        ArrayList<String> urlLists = new ArrayList<>();
        boolean isCompleteResource = true;

        if (TextUtils.equals(String.valueOf(baseAdSetting.getFormat()), Const.FORMAT.REWARDEDVIDEO_FORMAT)) {
            if (!TextUtils.isEmpty(iconUrl)) {
                urlLists.add(iconUrl);
            }

            if (!TextUtils.isEmpty(adChoiceUrl)) {
                urlLists.add(adChoiceUrl);
            }

            if (!TextUtils.isEmpty(endCardImageUrl)) {
                urlLists.add(endCardImageUrl);
            } else {
                isCompleteResource = false;
            }

            if (!TextUtils.isEmpty(videoUrl)) {
                urlLists.add(videoUrl);
            } else {
                isCompleteResource = false;
            }
        }

        if (TextUtils.equals(baseAdSetting.getFormat() + "", Const.FORMAT.INTERSTITIAL_FORMAT)) {
            if (!TextUtils.isEmpty(iconUrl)) {
                urlLists.add(iconUrl);
            }

            if (!TextUtils.isEmpty(adChoiceUrl)) {
                urlLists.add(adChoiceUrl);
            }

            if (!TextUtils.isEmpty(endCardImageUrl)) {
                urlLists.add(endCardImageUrl);
            } else {
                isCompleteResource = false;
            }

            if (isVideo()) {
                urlLists.add(videoUrl);
            }

        }


        if (isCompleteResource) {
            return urlLists;
        } else {
            return null;
        }

    }

    @Override
    public int getOfferSourceType() {
        return ADX_TYPE;
    }
}
