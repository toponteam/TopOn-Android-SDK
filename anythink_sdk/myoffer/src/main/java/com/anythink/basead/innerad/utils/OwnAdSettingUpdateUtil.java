/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.innerad.utils;

import com.anythink.basead.innerad.OwnBaseAdConfig;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.common.entity.OwnBaseAdSetting;

public class OwnAdSettingUpdateUtil {

    public static void update(OwnBaseAdSetting adxAdSetting, OwnBaseAdConfig ownAdConfig) {
        if (adxAdSetting == null || ownAdConfig == null) {
            return;
        }

        adxAdSetting.setVideoMute(ownAdConfig.isMute());
        adxAdSetting.setShowCloseTime(ownAdConfig.getShowCloseButtonTime());

        adxAdSetting.setBannerSize(ownAdConfig.getBannerSize());
        adxAdSetting.setIsShowCloseButton(ownAdConfig.getShowCloseButton());

        adxAdSetting.setSplashOrientation(ownAdConfig.getOrientation());
        adxAdSetting.setSplashCountdownTime(ownAdConfig.getCountDownTime());
        adxAdSetting.setCanSplashSkip(ownAdConfig.getCanSkip());
    }

    public static void update(BaseAdRequestInfo requestInfo, OwnBaseAdContent offerContent) {
        if (requestInfo == null || offerContent == null) {
            return;
        }

        BaseAdSetting adxAdSetting = requestInfo.baseAdSetting;
        if (adxAdSetting == null || !(requestInfo.baseAdSetting instanceof OwnBaseAdSetting)) {
            return;
        }

        BaseAdSetting newAdSetting = offerContent.getAdSetting();

        if (newAdSetting != null) {
            newAdSetting.setVideoMute(adxAdSetting.getVideoMute());
            newAdSetting.setShowCloseTime(adxAdSetting.getShowCloseTime());
            newAdSetting.setIsShowCloseButton(adxAdSetting.getIsShowCloseButton());

            newAdSetting.setBannerSize(adxAdSetting.getBannerSize());
            newAdSetting.setFormat(adxAdSetting.getFormat());

            newAdSetting.setSplashOrientation(adxAdSetting.getSplashOrientation());
            newAdSetting.setSplashCountdownTime(adxAdSetting.getSplashCountdownTime());
            newAdSetting.setCanSplashSkip(adxAdSetting.getCanSplashSkip());

            requestInfo.baseAdSetting = newAdSetting;
        } else {
            offerContent.setAdSetting((OwnBaseAdSetting) requestInfo.baseAdSetting);
        }
    }


}
