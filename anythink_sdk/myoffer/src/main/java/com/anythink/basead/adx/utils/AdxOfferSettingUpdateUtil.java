/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.adx.utils;

import com.anythink.basead.adx.AdxAdConfig;
import com.anythink.core.common.entity.AdxAdSetting;
import com.anythink.core.common.entity.AdxOffer;
import com.anythink.core.common.entity.AdxRequestInfo;

public class AdxOfferSettingUpdateUtil {

    public static void update(AdxAdSetting adxAdSetting, AdxAdConfig adxAdConfig) {
        if (adxAdSetting == null || adxAdConfig == null) {
            return;
        }

        adxAdSetting.setVideoMute(adxAdConfig.isMute());
        adxAdSetting.setShowCloseTime(adxAdConfig.getShowCloseButtonTime());
    }

    public static void update(AdxRequestInfo adxRequestInfo, AdxOffer adxOffer) {
        if (adxRequestInfo == null || adxOffer == null) {
            return;
        }

        AdxAdSetting adxAdSetting = adxRequestInfo.adxAdSetting;
        if (adxAdSetting == null) {
            return;
        }

        AdxAdSetting newAdxAdSetting = adxOffer.getAdxAdSetting();

        if (newAdxAdSetting != null) {
            newAdxAdSetting.setVideoMute(adxAdSetting.getVideoMute());
            newAdxAdSetting.setShowCloseTime(adxAdSetting.getShowCloseTime());
            newAdxAdSetting.setFormat(adxRequestInfo.adxAdSetting.getFormat());
            adxRequestInfo.adxAdSetting = newAdxAdSetting;
        } else {
            adxOffer.setAdxAdSetting(adxRequestInfo.adxAdSetting);
        }
    }


}
