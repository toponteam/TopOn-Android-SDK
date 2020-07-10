package com.anythink.china.api;

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
