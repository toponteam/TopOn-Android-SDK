/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.common.download;

public class ApkError {

    /***
     * Error Code
     */
    protected String code;
    /***
     * Error Message
     */
    protected String desc;

    protected ApkError(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String printStackTrace() {
        return "code[ " + code + " ],desc[ " + desc + " ]";
    }
}
