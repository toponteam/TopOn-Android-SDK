/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.base;


import com.anythink.BuildConfig;

public class Const {

    /**
     * SDK Version
     */
    public static final String SDK_VERSION_NAME = BuildConfig.SDK_VERSION_NAME;

    public static final int SDK_VERSION_DB = 4;

    public static final int CUSTOM_SDK_CODE = 0;

    public static final boolean DEBUG = false;

    public static final String SPU_LOCAL_USERAGENT = "local_ua";
    public static final String SPU_LOCAL_OS = "local_os";


    public static final int NET_TYPE_UNKNOW = -1;
    public static final int NET_TYPE_WIFI = -2;
    public static final int NET_TYPE_4G = -4444444;
    public static final int NET_TYPE_3G = -3333333;
    public static final int NET_TYPE_2G = -2222222;

    public static final int SYSTEM = 1;
    public static String RESOURCE_HEAD = "anythink"; //Head of Resource name

    public static final String SPU_NAME = RESOURCE_HEAD + "_sdk";
    public static final String SPU_APPID = RESOURCE_HEAD + "_appid";
    public static final String SPU_APPKEY = RESOURCE_HEAD + "_appkey";
    public static final String SPU_SYS_GAID = RESOURCE_HEAD + "_gaid";

    public static final String SPU_PLACEMENT_LOAD_RECORD_NAME = RESOURCE_HEAD + "_placement_load";
    public static final String SPU_CRASH_NAME = RESOURCE_HEAD + "_crash";

    public static final String HB_CACHE_FILE =  RESOURCE_HEAD + "_hb_cache_file";

    public static final String ONLINEAPI_SPU_FILE_NAME = RESOURCE_HEAD + "_onlineapi_file";

    public static final String SPU_EXC_LOG_NAME = "exc_log";

    /**
     * Own Offer Sharepreference File Name
     */
    public static final String SPU_ADX_FILE_NAME = RESOURCE_HEAD + "adx_file";
    public static final String SPU_OWN_OFFER_IMPRESSION_RECORD_FILE_NAME = RESOURCE_HEAD + "own_offerid_impression";

    /**
     * Placement update check
     */
    public static final String SPU_PLACEMENT_STRATEGY_UPDATE_CHECK_NAME = RESOURCE_HEAD + "_placement_strategy_update_check";

    public static class NETWORK_FIRM {
        public static final int ADX_NETWORK_FIRM_ID = 66;
        public static final int MINTEGRAL_ONLINE = 41;
        public static final int GDT_ONLINE = 42;
    }

    public static class NETWORK_REQUEST_PARAMS_KEY {
        public static final String BASE_AD_PARAMS_KEY = "basead_params";
        public static final String BID_PAYLOAD_KEY = "payload";
        public static final String REQUEST_AD_NUM = "request_ad_num";
        public static final String APP_CCPA_SWITCH_KEY = "app_ccpa_switch";
        public static final String APP_COPPA_SWITCH_KEY = "app_coppa_switch";
    }

    public static class FORMAT {
        public static final String NATIVE_FORMAT = "0";
        public static final String REWARDEDVIDEO_FORMAT = "1";
        public static final String BANNER_FORMAT = "2";
        public static final String INTERSTITIAL_FORMAT = "3";
        public static final String SPLASH_FORMAT = "4";
    }

    public static class FORMAT_STRING {
        public static final String NATIVE = "Native";
        public static final String REWARDEDVIDEO = "RewardedVideo";
        public static final String BANNER = "Banner";
        public static final String INTERSTITIAL = "Interstitial";
        public static final String SPLASH = "Splash";
    }

    public static class DEFAULT_SDK_KEY {
        /***
         * Outdate time of AppSetting
         */
        public static final long APPSTRATEGY_DEFAULT_OUTTIME = 2 * 60 * 60 * 1000;

    }

    public static class SPUKEY {
        public static final String SPU_APP_STRATEGY_TYPE = "AP_SY";
        public static final String SPU_PLACEMENT_STRATEGY_TYPE = "PL_SY";
        public static final String SPU_PLACE_ID_TYPE = "SPU_PLACE_ID_TYPE";
        public static final String SPU_UPLOAD_DATA_LEVEL = "UPLOAD_DATA_LEVEL";
        public static final String SPU_NETWORK_VERSION_NAME = "NETWORK_VERSION_NAME";

        public static final String SPU_PSID_KEY = "SPU_PSID_KEY";
        public static final String SPU_SESSIONID_KEY = "SPU_SESSIONID_KEY";
        public static final String SPU_INIT_TIME_KEY = "SPU_INIT_TIME_KEY";
        public static final String SPU_UP_ID_KEY = "UP_ID";
        public static final String SPU_EU_INFO = "EU_INFO";

        public static final String SPU_FIRST_INIT_TIME = "AT_INIT_TIME";


        /**
         * Only for exc_log
         **/
        public static final String SPU_EXC_SYS = "exc_sys";
        public static final String SPU_EXC_BK = "exc_bk";

        /**
         * For Own Ad
         */
        public static final String SPU_OWN_AD_SUFFIX_WIN_NOTICE = "_win_notice";

    }


    /**
     * API Request
     */
    public static class API {
        public static final String APPSTR_APIVERSION = "1.0";
        public static final String JSON_STATUS = "code";
        public static final int JSON_RESPONSE_STATUS_SUCCESS = 0;
        public static String JSON_DATA = "data";

        public static final String URL_APP_STRATEGY = "https://api.anythinktech.com/v1/open/app";
        public static final String URL_PLACE_STRATEGY = "https://api.anythinktech.com/v1/open/placement";
        public static final String URL_AGENT = "https://da.anythinktech.com/v1/open/da";
        public static final String URL_TRACKING_STRATEGY = "https://tk.anythinktech.com/v1/open/tk";
        public static final String URL_TRAFFIC_CHECK = "https://api.anythinktech.com/v1/open/eu";
        public static final String URL_HEADBIDDING = "https://adx.anythinktech.com/bid";
        public static final String URL_ADX_REQUEST = "https://adx.anythinktech.com/request";
        public static final String URL_ADX_TK = "https://adxtk.anythinktech.com/v1";
        public static final String URL_ONLINE_API_REQUEST = "https://adx.anythinktech.com/openapi/req";
        public static final String URL_FACEBOOK_INHOUSE_DOMAIN = "https://bidding.anythinktech.com";
        /**
         * Test API
         **/
        //TODO TEST
//        public static final String URL_APP_STRATEGY = "http://test.aa.toponad.com/v1/open/app";
//        public static final String URL_PLACE_STRATEGY = "http://test.aa.toponad.com/v1/open/placement";
//        public static final String URL_AGENT = "http://test.dd.toponad.com/v1/open/da";
//        public static final String URL_TRACKING_STRATEGY = "http://test.tt.toponad.com/v1/open/tk";
//        public static final String URL_TRAFFIC_CHECK = "http://test.aa.toponad.com/v1/open/eu";
//        public static final String URL_HEADBIDDING = "http://test.adx.anythinktech.com/bid";
//        public static final String URL_ADX_REQUEST = "http://test.adx.anythinktech.com/request";
//        public static final String URL_ADX_TK = "http://test.adxtk.anythinktech.com/v1";
//        public static final String URL_ONLINE_API_REQUEST = "http://test.adx.anythinktech.com/openapi/req";
//        public static final String URL_FACEBOOK_INHOUSE_DOMAIN = "http://47.241.25.167:8080";


    }


    public static class URL {
        public static final String GDPR_URL = "https://img.anythinktech.com/gdpr/PrivacyPolicySetting.html";
    }

    public static class LOGKEY {
        public static String REQUEST = "request";
        public static String REQUEST_RESULT = "request_result";
        public static String IMPRESSION = "impression";
        public static String CLICK = "click";
        public static String CLOSE = "close";

        public static String SUCCESS = "success";
        public static String FAIL = "fail";
        public static String START = "start";


        public static String API_BANNER = "banner";
        public static String API_INTERSTITIAL = "inter";
        public static String API_REWARD = "reward";
        public static String API_NATIVE = "native";
        public static String API_SPLASH = "splash";

        public static String API_LOAD = "load";
        public static String API_LOAD_RESULT = "load_result";
        public static String API_SHOW = "show";
        public static String API_ISREADY = "isready";
        public static String API_AD_STATUS = "status";


        public static String HEADBIDDING = "headbidding";
        public static String STRATEGY = "strategy";


    }


}
