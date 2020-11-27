/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.entity;

public class OfferErrorCode {

    public final static String unknow = "-9999";

    public static final String exception = "10000";
    public final static String httpStatuException = "10001";

    public final static String timeOutError = "20001";
    public final static String outOfCapError = "20003";
    public final static String inPacingError = "20004";
    public final static String loadingError = "20005";
    public final static String exclueOfferError = "20006";

    public final static String noADError = "30001";
    public final static String noSettingError = "30002";

    public final static String rewardedVideoPlayError = "40002";
    public final static String incompleteResourceError = "30003";




    public static final String fail_load_timeout = "Load timeout!";
    public static final String fail_offer_loading = "Offer data is loading.";
    public static final String fail_save = "Save fail!";
    public static final String fail_load_cannel = "Load cancel!";
    public static final String fail_connect = "Http connect error!";
    public static final String fail_params = "offerid、placementid can not be null!";
    public static final String fail_params_adx = "bidid、placementid can not be null!";
    public static final String fail_no_offer = "No fill, offer = null!";
    public static final String fail_no_setting = "No fill, setting = null!";
    public static final String fail_out_of_cap = "Ad is out of cap!";
    public static final String fail_in_pacing = "Ad is in pacing!";
    public static final String fail_null_context = "context = null!";
    public static final String fail_player = "Video player error!";
    public static final String fail_no_video_url = "Video url no exist!";
    public static final String fail_video_file_error_ = "Video file error!";
    public static final String fail_incomplete_resource = "Incomplete resource allocation!";
    public static final String fail_in_exclude_offer = "The cross-promotion offer was filtered for exclude offers.";


    public static OfferError get(String code, String msg) {
        return new OfferError(code, msg);
    }

}
