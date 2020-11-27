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

/**
 * Adx Ad Config
 */
public class AdxAdSetting extends BaseAdSetting {

    private int clickmode; //1：Async Click 0：Sync Click
    private int ipua;//1=webview ua 2=system ua。default=1
    private int clua;//1=webview ua 2=system ua。default=1

    public AdxAdSetting() {
    }

    public int getClickmode() {
        return clickmode;
    }

    public void setClickmode(int clickmode) {
        this.clickmode = clickmode;
    }

    public int getIpua() {
        return ipua;
    }

    public void setIpua(int ipua) {
        this.ipua = ipua;
    }

    public int getClua() {
        return clua;
    }

    public void setClua(int clua) {
        this.clua = clua;
    }

    public static AdxAdSetting parseAdxSetting(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        AdxAdSetting adxSetting = new AdxAdSetting();
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject jsonObject = new JSONObject(json);

                adxSetting.setFormat(jsonObject.optInt("f_t"));
                //Mapping to common setting
                int v_c = jsonObject.optInt("v_c");
                switch (v_c) {
                    case 1:
                        v_c = 0;
                        break;
                    case 2:
                        v_c = 1;
                        break;
                }
                adxSetting.setVideoClick(v_c);

                adxSetting.setShowBannerTime(jsonObject.optInt("s_b_t"));

                //Mapping to common setting
                int e_c_a = jsonObject.optInt("e_c_a");
                switch (e_c_a) {
                    case 1:
                        e_c_a = 0;
                        break;
                    case 2:
                        e_c_a = 1;
                        break;
                    case 3:
                        e_c_a = 2;
                        break;
                }
                adxSetting.setEndCardClickArea(e_c_a);

                //Mapping to common setting
                int ak_cfm = jsonObject.optInt("ak_cfm");
                switch (ak_cfm) {
                    case 1:
                        ak_cfm = 0;
                        break;
                    case 2:
                        ak_cfm = 1;
                        break;
                }
                adxSetting.setApkDownloadConfirm(ak_cfm);

                adxSetting.setOfferTimeout(jsonObject.optInt("m_t"));

//                adxSetting.setSplashCountdownTime(jsonObject.optLong("ctdown_time"));
//                adxSetting.setCanSplashSkip(jsonObject.optInt("sk_able"));
//                adxSetting.setSplashOrientation(jsonObject.optInt("orient"));
//                adxSetting.setBannerSize(jsonObject.optString("size"));
//                adxSetting.setIsShowCloseButton(jsonObject.optInt("cl_btn"));

                //Mapping to common setting
                int cm = jsonObject.optInt("cm");
                switch (cm) {
                    case 1:
                        cm = 0;
                        break;
                    case 2:
                        cm = 1;
                        break;
                }
                adxSetting.setClickmode(cm);

                adxSetting.setIpua(jsonObject.optInt("ipua"));
                adxSetting.setClua(jsonObject.optInt("clua"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return adxSetting;
    }


}
