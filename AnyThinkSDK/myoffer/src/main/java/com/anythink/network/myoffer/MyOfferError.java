package com.anythink.network.myoffer;

public class MyOfferError {

    /***
     * Error Code
     */
    protected String code;
    /***
     * Error Message
     */
    protected String desc;

    protected MyOfferError(String code, String desc) {
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
