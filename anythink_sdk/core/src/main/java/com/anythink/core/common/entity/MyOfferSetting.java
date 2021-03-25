/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MyOfferSetting extends BaseAdSetting {

    public MyOfferSetting() {

    }


    public static MyOfferSetting parseMyOfferSetting(String json) {
        MyOfferSetting myOfferSetting = new MyOfferSetting();
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject jsonObject = new JSONObject(json);

                myOfferSetting.setFormat(jsonObject.optInt("f_t"));
                myOfferSetting.setVideoClick(jsonObject.optInt("v_c"));
                myOfferSetting.setShowBannerTime(jsonObject.optInt("s_b_t"));
                myOfferSetting.setEndCardClickArea(jsonObject.optInt("e_c_a"));
                myOfferSetting.setVideoMute(jsonObject.optInt("v_m"));
                myOfferSetting.setShowCloseTime(jsonObject.optInt("s_c_t"));
                myOfferSetting.setOfferTimeout(jsonObject.optInt("m_t"));
                myOfferSetting.setOfferCacheTime(jsonObject.optLong("o_c_t"));

                myOfferSetting.setApkDownloadConfirm(jsonObject.optInt("ak_cfm"));

                myOfferSetting.setSplashCountdownTime(jsonObject.optLong("ctdown_time"));
                myOfferSetting.setCanSplashSkip(jsonObject.optInt("sk_able"));
                myOfferSetting.setSplashOrientation(jsonObject.optInt("orient"));
                myOfferSetting.setBannerSize(jsonObject.optString("size"));
                myOfferSetting.setIsShowCloseButton(jsonObject.optInt("cl_btn"));

                //v5.7.9
                myOfferSetting.setProbabilityForDelayShowCloseButtonInEndCard(jsonObject.optInt("ec_r"));
                myOfferSetting.setMinDelayTimeWhenShowCloseButton(jsonObject.optInt("ec_s_t"));
                myOfferSetting.setMaxDelayTimeWhenShowCloseButton(jsonObject.optInt("ec_l_t"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return myOfferSetting;
    }
}
