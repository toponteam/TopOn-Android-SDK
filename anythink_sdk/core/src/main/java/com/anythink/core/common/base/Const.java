package com.anythink.core.common.base;


import com.anythink.BuildConfig;

public class Const {

    /**
     * SDK Version
     */
    public static final String SDK_VERSION_NAME = BuildConfig.SDK_VERSION_NAME;

    public static final int SDK_VERSION_DB = 4;

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

    /**
     * Placement update check
     */
    public static final String SPU_PLACEMENT_STRATEGY_UPDATE_CHECK_NAME = RESOURCE_HEAD + "_placement_strategy_update_check";

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
    }


    /**
     * API Request
     */
    public static class API {
        public static final String APPSTR_APIVERSION = "1.0";
        public static final String JSON_STATUS = "code";
        public static final int JSON_RESPONSE_STATUS_SUCCESS = 0;
        public static String JSON_DATA = "data";

        public static final String URL_APP_STRATEGY = BuildConfig.URL_APP_STRATEGY;
        public static final String URL_PLACE_STRATEGY = BuildConfig.URL_PLACE_STRATEGY;
        public static final String URL_AGENT = BuildConfig.URL_AGENT;
        public static final String URL_TRACKING_STRATEGY = BuildConfig.URL_TRACKING_STRATEGY;
        public static final String URL_TRAFFIC_CHECK = BuildConfig.URL_TRAFFIC_CHECK;

        /**Test API**/
//        public static final String URL_APP_STRATEGY = "http://test.aa.toponad.com/v1/open/app";
//        public static final String URL_PLACE_STRATEGY = "http://test.aa.toponad.com/v1/open/placement";
//        public static final String URL_AGENT = "http://test.dd.toponad.com/v1/open/da";
//        public static final String URL_TRACKING_STRATEGY = "http://test.tt.toponad.com/v1/open/tk";
//        public static final String URL_TRAFFIC_CHECK = "http://test.aa.toponad.com/v1/open/eu";

    }


    public static class URL {
        public static final String GDPR_URL = BuildConfig.GDPR_URL;
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
        public static String API_SHOW = "show";
        public static String API_ISREADY = "isready";


        public static String HEADBIDDING = "headbidding";
        public static String STRATEGY = "strategy";


    }


}
