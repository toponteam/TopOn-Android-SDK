/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.china.common.download;

public class ApkErrorCode {

    public final static String unknow = "-9999";

    public static final String exception = "10000";
    public final static String httpStatuException = "10001";

    public final static String timeOutError = "20001";

    public static final String fail_save = "Save fail!";
    public static final String fail_connect = "Http connect error!";


    public static ApkError get(String code, String msg) {
        return new ApkError(code, msg);
    }

}
