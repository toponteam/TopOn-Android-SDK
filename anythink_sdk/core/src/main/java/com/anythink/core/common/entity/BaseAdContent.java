/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;


import android.text.TextUtils;

import java.io.Serializable;
import java.util.List;

public abstract class BaseAdContent<T extends BaseAdSetting> implements Serializable {
    protected static final int MYOFFER_TYPE = 1;
    protected static final int ADX_TYPE = 2;

    protected String offerId; //OfferId
    protected String creativeId; //Resource Id
    protected String title; //Offer Title
    protected String desc; //Offer Description
    protected String iconUrl; //Offer icon url
    protected String mainImageUrl; //Offer image url
    protected String endCardImageUrl; //Offer's EndCard image url（Just use by RewardedVideo and Interstitial）
    protected String adChoiceUrl; //AdSource icon url
    protected String ctaText; //Click to action text
    protected String videoUrl; //Video url
    protected String previewUrl; //Preview Click url
    protected String deeplinkUrl; //Deep Link Click url
    protected String clickUrl; //Click url
    protected String pkgName; //Package Name

    protected int resourceType; //1:Video，2:Image
    protected int clickType; //ClickType：1：Market，2：Browser


    protected BaseAdContent() {

    }

    public abstract List<String> getUrlList(T baseAdSetting);


    public final String getOfferId() {
        return offerId;
    }

    public final void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public final String getCreativeId() {
        return creativeId;
    }

    public final void setCreativeId(String creativeId) {
        this.creativeId = creativeId;
    }

    public final String getTitle() {
        return title;
    }

    public final void setTitle(String title) {
        this.title = title;
    }

    public final String getDesc() {
        return desc;
    }

    public final void setDesc(String desc) {
        this.desc = desc;
    }

    public final String getIconUrl() {
        return iconUrl;
    }

    public final void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public final String getMainImageUrl() {
        return mainImageUrl;
    }

    public final void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public final String getEndCardImageUrl() {
        return endCardImageUrl;
    }

    public final void setEndCardImageUrl(String endCardImageUrl) {
        this.endCardImageUrl = endCardImageUrl;
    }

    public final String getAdChoiceUrl() {
        return adChoiceUrl;
    }

    public final void setAdChoiceUrl(String adChoiceUrl) {
        this.adChoiceUrl = adChoiceUrl;
    }

    public final String getCtaText() {
        return ctaText;
    }

    public final void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public final String getVideoUrl() {
        return videoUrl;
    }

    public final void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }


    public final String getPreviewUrl() {
        return previewUrl;
    }

    public final void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public final String getDeeplinkUrl() {
        return deeplinkUrl;
    }

    public final void setDeeplinkUrl(String deeplinkUrl) {
        this.deeplinkUrl = deeplinkUrl;
    }

    public final String getClickUrl() {
        return clickUrl;
    }

    public final void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }


    public final String getPkgName() {
        return pkgName;
    }

    public final void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }


    public final int getResourceType() {
        return resourceType;
    }

    public final void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public int getClickType() {
        return clickType;
    }

    public void setClickType(int clickType) {
        this.clickType = clickType;
    }

    public boolean isVideo() {
        return !TextUtils.isEmpty(videoUrl);
    }

    public abstract int getOfferSourceType();
}
