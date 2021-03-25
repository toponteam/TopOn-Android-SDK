/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import java.io.Serializable;

public abstract class BaseAdSetting implements Serializable {

    private int format;//ad type： 0 =Native，1= RV，2=banner ，3=inter ，4=splash
    private int videoClick;//Video click handl, 0: No response,  1: Open market or download apk
    private int showBannerTime;//Banner Show Time, -1: User click to show, -2: No show,  0: Show banner when video start
    private int endCardClickArea;//EndCard Click area, 0: Fullscreen (Default）, 1: CTA button, 2: Banner Area
    private int videoMute;//Sound mode, 0: Mute， 1：System volume
    private int showCloseTime;//Close show time

    private int apkDownloadConfirm;//1:true, 0:false
    private int canSplashSkip;//0:true,1:false
    private long splashCountdownTime; //Splash Countdown Time
    private int splashOrientation; //1:portrait, 2:landscape

    private String bannerSize; //320x50,320x90,300x250,728x90
    private int isShowCloseButton; //1:true, 2:false

    private int offerTimeout;//Offer resource download timeout
    private long offerCacheTime;//Offer Cache time, Default: 604800ms (include Apk)

    /**
     * 1：Only Deeplink Click (Default）
     * 2：Click url before Deeplink Click
     * 3：Click url after Deeplink Click
     */
    private int deeplinkMode;

    /**
     * 1: Browser
     * 2: Inner Browser
     */
    private int loadType;

    public static final String BANNER_SIZE_320x50 = "320x50";
    public static final String BANNER_SIZE_320x90 = "320x90";
    public static final String BANNER_SIZE_300x250 = "300x250";
    public static final String BANNER_SIZE_728x90 = "728x90";

    /**
     * Add by 5.7.9
     */
    protected int probabilityForDelayShowCloseButtonInEndCard;
    protected int MinDelayTimeWhenShowCloseButton;
    protected int MaxDelayTimeWhenShowCloseButton;


    public int getLoadType() {
        if (loadType == 0) {
            // Default
            return 1;
        }
        return loadType;
    }

    public void setLoadType(int loadType) {
        this.loadType = loadType;
    }

    public int getDeeplinkMode() {
        //Default
        if (deeplinkMode == 0) {
            return 1;
        }
        return deeplinkMode;
    }

    public void setDeeplinkMode(int deeplinkMode) {
        this.deeplinkMode = deeplinkMode;
    }

    public final int getOfferTimeout() {
        return offerTimeout;
    }

    public final void setOfferTimeout(int offerTimeout) {
        this.offerTimeout = offerTimeout;
    }

    public final long getSplashCountdownTime() {
        return splashCountdownTime;
    }

    public final void setSplashCountdownTime(long splashCountdownTime) {
        this.splashCountdownTime = splashCountdownTime;
    }

    public final int getApkDownloadConfirm() {
        return apkDownloadConfirm;
    }

    public final void setApkDownloadConfirm(int apkDownloadConfirm) {
        this.apkDownloadConfirm = apkDownloadConfirm;
    }

    /**
     * 0: can skip <p>
     * 1: can not skip
     */
    public final int getCanSplashSkip() {
        return canSplashSkip;
    }

    public final void setCanSplashSkip(int canSplashSkip) {
        this.canSplashSkip = canSplashSkip;
    }

    /**
     * 1: portrait <p>
     * 2: landscape
     */
    public final int getSplashOrientation() {
        return splashOrientation;
    }

    public final void setSplashOrientation(int splashOrientation) {
        this.splashOrientation = splashOrientation;
    }

    public final String getBannerSize() {
        return bannerSize;
    }

    public final void setBannerSize(String bannerSize) {
        this.bannerSize = bannerSize;
    }

    /**
     * 0: show close button <p>
     * 1: do't show close button
     */
    public final int getIsShowCloseButton() {
        return isShowCloseButton;
    }

    public final void setIsShowCloseButton(int isShowCloseButton) {
        this.isShowCloseButton = isShowCloseButton;
    }

    public final int getFormat() {
        return format;
    }

    public final void setFormat(int format) {
        this.format = format;
    }

    public final int getVideoClick() {
        return videoClick;
    }

    public final void setVideoClick(int videoClick) {
        this.videoClick = videoClick;
    }

    public final int getShowBannerTime() {
        return showBannerTime;
    }

    public final void setShowBannerTime(int showBannerTime) {
        this.showBannerTime = showBannerTime;
    }

    public final int getEndCardClickArea() {
        return endCardClickArea;
    }

    public final void setEndCardClickArea(int endCardClickArea) {
        this.endCardClickArea = endCardClickArea;
    }

    public final int getVideoMute() {
        return videoMute;
    }

    public final void setVideoMute(int videoMute) {
        this.videoMute = videoMute;
    }

    public final int getShowCloseTime() {
        return showCloseTime;
    }

    public final void setShowCloseTime(int showCloseTime) {
        this.showCloseTime = showCloseTime;
    }


    public long getOfferCacheTime() {
        return offerCacheTime;
    }

    public void setOfferCacheTime(long offerCacheTime) {
        this.offerCacheTime = offerCacheTime;
    }

    public int getProbabilityForDelayShowCloseButtonInEndCard() {
        return probabilityForDelayShowCloseButtonInEndCard;
    }

    public void setProbabilityForDelayShowCloseButtonInEndCard(int probabilityForDelayShowCloseButtonInEndCard) {
        this.probabilityForDelayShowCloseButtonInEndCard = probabilityForDelayShowCloseButtonInEndCard;
    }

    public int getMinDelayTimeWhenShowCloseButton() {
        return MinDelayTimeWhenShowCloseButton;
    }

    public void setMinDelayTimeWhenShowCloseButton(int minDelayTimeWhenShowCloseButton) {
        MinDelayTimeWhenShowCloseButton = minDelayTimeWhenShowCloseButton;
    }

    public int getMaxDelayTimeWhenShowCloseButton() {
        return MaxDelayTimeWhenShowCloseButton;
    }

    public void setMaxDelayTimeWhenShowCloseButton(int maxDelayTimeWhenShowCloseButton) {
        MaxDelayTimeWhenShowCloseButton = maxDelayTimeWhenShowCloseButton;
    }
}