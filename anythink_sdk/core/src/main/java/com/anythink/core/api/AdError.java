/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.api;

import android.text.TextUtils;



public class AdError {
    /***
     * Error Code
     */
    protected String code;
    /***
     * Error Message
     */
    protected String desc;

    /**
     * Mediation Error Info
     */
    protected String platformCode;
    protected String platformMSG;

    protected String itemsErrorInfo;

    protected AdError(String code, String desc, String platformCode, String platformMSG) {
        this.code = code;
        this.desc = desc;
        this.platformCode = platformCode;
        this.platformMSG = platformMSG;
        this.itemsErrorInfo = "";
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public String getPlatformMSG() {
        return platformMSG;
    }

    public String printStackTrace() {
        return "code:[ " + code + " ]" +
                "desc:[ " + desc + " ]" +
                "platformCode:[ " + platformCode + " ]" +
                "platformMSG:[ " + platformMSG + " ]";
    }


    public void putNetworkErrorMsg(String adsourceId, int networkFirmId, String networkName, AdError adError) {
        //Only for old version
        platformCode = adError.platformCode;
        platformMSG = adError.platformMSG;

        itemsErrorInfo = itemsErrorInfo +
                "\n  { ad_source_id[ " + adsourceId + " ];network_firm_id[ " + networkFirmId + " ];network_name=[ " + networkName + " ];network_error:[ " + adError.printStackTrace() + " ] }";
    }

    public String getFullErrorInfo() {
        if (TextUtils.isEmpty(itemsErrorInfo)) {
            return "code:[ " + code + " ]" +
                    "desc:[ " + desc + " ]" +
                    "platformCode:[ " + platformCode + " ]" +
                    "platformMSG:[ " + platformMSG + " ]";
        } else {
            String errorInfo =
                    "\ncode[ " + code + " ]" +
                            "\ndesc[ " + desc + " ]" +
                            "\ndetail[ " + itemsErrorInfo + " \n]";
            return errorInfo;
        }
    }


    @Override
    public String toString() {
        return printStackTrace();
    }
}
