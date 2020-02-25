package com.anythink.network.myoffer;

public class MyOfferErrorCode {

    public final static String unknow = "-9999";

    public static final String exception = "10000";
    public final static String httpStatuException = "10001";

    public final static String timeOutError = "20001";
    public final static String outOfCapError = "20003";
    public final static String inPacingError = "20004";

    public final static String noADError = "30001";
    public final static String noSettingError = "30002";

    public final static String rewardedVideoPlayError = "40002";




    public static final String fail_load_timeout = "Load timeout!";
    public static final String fail_save = "Save fail!";
    public static final String fail_load_cannel = "Load cancel!";
    public static final String fail_connect = "Http connect error!";
    public static final String fail_params = "offerid、placementid can not be null!";
    public static final String fail_no_offer = "No fill, offer = null!";
    public static final String fail_no_setting = "No fill, setting = null!";
    public static final String fail_out_of_cap = "Ad is out of cap!";
    public static final String fail_in_pacing = "Ad is in pacing!";
    public static final String fail_null_context = "context = null!";
    public static final String fail_player = "Video player error!";
    public static final String fail_no_video_url = "Video url no exist!";
    public static final String fail_video_file_error_ = "Video file error!";


    public static MyOfferError get(String code, String msg) {
        return new MyOfferError(code, msg);
    }

}
