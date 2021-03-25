/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad;

public class OwnBaseAdConfig {

    private int mIsMute;
    private int mShowCloseButtonTime;
    private int mShowCloseButton;
    private String mBannerSize;
    private int mOrientation;
    private int mCountdownTime;
    private int mCanSkip;

    public static class Builder {
        private int mIsMute;
        private int mShowCloseButtonTime;
        private int mShowCloseButton;
        private String mBannerSize;
        private int mOrientation;
        private int mCountDown;
        private int mAllowsSkip;

        public Builder isMute(int isMute) {
            this.mIsMute = isMute;
            return this;
        }

        public Builder showCloseButtonTime(int showCloseButtonTime) {
            this.mShowCloseButtonTime = showCloseButtonTime;
            return this;
        }

        public Builder showCloseButton(int showCloseButton) {
            this.mShowCloseButton = showCloseButton;
            return this;
        }

        public Builder bannerSize(String bannerSize) {
            this.mBannerSize = bannerSize;
            return this;
        }

        public Builder orientation(int orientation) {
            this.mOrientation = orientation;
            return this;
        }

        public Builder countdownTime(int countdown) {
            this.mCountDown = countdown;
            return this;
        }

        public Builder canSkip(int canSkip) {
            this.mAllowsSkip = canSkip;
            return this;
        }

        public OwnBaseAdConfig build() {
            OwnBaseAdConfig adxAdConfig = new OwnBaseAdConfig();
            adxAdConfig.mIsMute = this.mIsMute;
            adxAdConfig.mShowCloseButtonTime = this.mShowCloseButtonTime;
            adxAdConfig.mShowCloseButton = this.mShowCloseButton;
            adxAdConfig.mBannerSize = this.mBannerSize;
            adxAdConfig.mOrientation = this.mOrientation;
            adxAdConfig.mCountdownTime = this.mCountDown;
            adxAdConfig.mCanSkip = this.mAllowsSkip;
            return adxAdConfig;
        }
    }

    public int isMute() {
        return mIsMute;
    }

    public int getShowCloseButtonTime() {
        return mShowCloseButtonTime;
    }

    public int getShowCloseButton() {
        return mShowCloseButton;
    }

    public String getBannerSize() {
        return mBannerSize;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public int getCountDownTime() {
        return mCountdownTime;
    }

    public int getCanSkip() {
        return mCanSkip;
    }
}
