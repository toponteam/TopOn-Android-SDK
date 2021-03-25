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

public abstract class OwnBaseAdContent extends BaseAdContent<OwnBaseAdSetting> {

    long videoLength;
    String videoScreen;
    String endcardUrl;

    int creativeType; //Creative type
    public static final int CREATIVE_TYPE_SINGLE_PICTURE = 1;
    public static final int CREATIVE_TYPE_GROUP_PICTURES = 2;
    public static final int CREATIVE_TYPE_SINGLE_PICTURE_AND_TEXT = 3;
    public static final int CREATIVE_TYPE_GROUP_PICTURES_AND_TEXT = 4;
    public static final int CREATIVE_TYPE_VIDEO = 5;
    public static final int CREATIVE_TYPE_XHTML = 6;

    String imageUrlList;
    String bannerXhtml;

    OwnBaseAdSetting adxAdSetting;
    OwnBaseAdTrackObject adxTrackObject;

    public OwnBaseAdSetting getAdSetting() {
        return adxAdSetting;
    }

    public void setAdSetting(OwnBaseAdSetting adxAdSetting) {
        this.adxAdSetting = adxAdSetting;
    }

    public OwnBaseAdTrackObject getTrackObject() {
        return adxTrackObject;
    }

    public void setTrackObject(OwnBaseAdTrackObject adxTrackObject) {
        this.adxTrackObject = adxTrackObject;
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

    public int getCreativeType() {
        return this.creativeType;
    }

    public void setCreativeType(int crt_type) {
        this.creativeType = crt_type;
    }

    ;

    public String getImageUrlList() {
        return this.imageUrlList;
    }

    public void setImageUrlList(String imageUrlList) {
        this.imageUrlList = imageUrlList;
    }

    ;

    public String getBannerXhtml() {
        return this.bannerXhtml;
    }

    public void setBannerXhtml(String bannerXhtml) {
        this.bannerXhtml = bannerXhtml;
    }

    ;

    @Override
    public List<String> getUrlList(OwnBaseAdSetting baseAdSetting) {
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

            if (hasVideoUrl()) {
                urlLists.add(videoUrl);
            }

        }

        if (TextUtils.equals(baseAdSetting.getFormat() + "", Const.FORMAT.BANNER_FORMAT)) {

            switch (getCreativeType()) {
                case OwnBaseAdContent.CREATIVE_TYPE_SINGLE_PICTURE:
                    if (!TextUtils.isEmpty(endCardImageUrl)) {
                        urlLists.add(endCardImageUrl);
                    } else {
                        isCompleteResource = false;
                    }
                    break;
                case OwnBaseAdContent.CREATIVE_TYPE_SINGLE_PICTURE_AND_TEXT:
                    if (TextUtils.equals(MyOfferSetting.BANNER_SIZE_320x50, baseAdSetting.getBannerSize())) {
                        if (!TextUtils.isEmpty(iconUrl)) {
                            urlLists.add(iconUrl);
                        } else {
                            isCompleteResource = false;
                        }
                    } else {
                        if (!TextUtils.isEmpty(endCardImageUrl)) {
                            urlLists.add(endCardImageUrl);
                        } else {
                            isCompleteResource = false;
                        }
                    }
                    break;
                case OwnBaseAdContent.CREATIVE_TYPE_GROUP_PICTURES:
                case OwnBaseAdContent.CREATIVE_TYPE_GROUP_PICTURES_AND_TEXT:
                    if (!TextUtils.isEmpty(imageUrlList)) {
                        //todo don't support in this version
                    } else {
                        isCompleteResource = false;
                    }
                    break;
                default:
                    break;
            }

            if (!TextUtils.isEmpty(adChoiceUrl)) {
                urlLists.add(adChoiceUrl);
            }

        }

//        if (TextUtils.equals(baseAdSetting.getFormat() + "", Const.FORMAT.NATIVE_FORMAT)) {
//
//        }

        if (TextUtils.equals(String.valueOf(baseAdSetting.getFormat()), Const.FORMAT.SPLASH_FORMAT)) {

            if (OwnBaseAdContent.CREATIVE_TYPE_SINGLE_PICTURE != getCreativeType()) {
                if (!TextUtils.isEmpty(iconUrl)) {
                    urlLists.add(iconUrl);
                }
            }

            if (!TextUtils.isEmpty(adChoiceUrl)) {
                urlLists.add(adChoiceUrl);
            }

            if (!TextUtils.isEmpty(endCardImageUrl)) {
                urlLists.add(endCardImageUrl);
            } else {
                isCompleteResource = false;
            }

        }


        if (isCompleteResource) {
            return urlLists;
        } else {
            return null;
        }

    }
}
