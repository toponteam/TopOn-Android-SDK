/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.adx;

public class AdxAdConfig {

    private int mIsMute;
    private int mShowCloseButtonTime;

    public static class Builder {
        private int mIsMute;
        private int mShowCloseButtonTime;

        public Builder isMute(int isMute) {
            this.mIsMute = isMute;
            return this;
        }

        public Builder showCloseButtonTime(int showCloseButtonTime) {
            this.mShowCloseButtonTime = showCloseButtonTime;
            return this;
        }

        public AdxAdConfig build() {
            AdxAdConfig adxAdConfig = new AdxAdConfig();
            adxAdConfig.mIsMute = this.mIsMute;
            adxAdConfig.mShowCloseButtonTime = this.mShowCloseButtonTime;
            return adxAdConfig;
        }
    }

    public int isMute() {
        return mIsMute;
    }

    public int getShowCloseButtonTime() {
        return mShowCloseButtonTime;
    }
}
