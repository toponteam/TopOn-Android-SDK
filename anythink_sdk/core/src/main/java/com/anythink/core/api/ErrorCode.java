package com.anythink.core.api;

/**
 * 错误码
 * Created by zhou on 2018/1/13.
 */

public class ErrorCode {

    public final static String unknown = "-9999";

    public final static String exception = "9999";
    public final static String httpStatuException = "9990";
    public final static String statuError = "9991";
    public final static String dataLevelLowError = "9992";

    public final static String networkError = "1001";
    public final static String serverError = "1002";

    public final static String timeOutError = "2001";
    public final static String adapterNotExistError = "2002";
    public final static String outOfCapError = "2003";
    public final static String inPacingError = "2004";
    public final static String loadingError = "2005";
    public final static String adapterInnerError = "2006";
    public final static String inRequestFailPacing = "2007";
    public final static String loadFailInPacingError = "2008";
    public final static String loadCappingError = "2009";

    public final static String placeStrategyError = "3001";
    public final static String appIdOrPlaceIdEmpty = "3002";
    public final static String formatError = "3003";

    public final static String noADError = "4001";
    public final static String contextDestoryError = "4002";
    public final static String placementAdClose = "4003";
    public final static String noAdsourceConfig = "4004";
    public final static String noVailAdsource = "4005";
    public final static String rewardedVideoPlayError = "4006";

    public final static String appKeyError = "10001";
    public final static String appIdError = "10003";
    public final static String placementIdError = "10004";




    /***
     * Error Message
     * @param code
     * @return
     */
    public static AdError getErrorCode(String code, String platformCode, String platformMsg) {
        switch (code) {
            case exception:
                return new AdError(exception, "Exception in sdk.", platformCode, platformMsg);
            case httpStatuException:
                return new AdError(httpStatuException, "Https status exception.", platformCode, platformMsg);
            case statuError:
                return new AdError(statuError, "Service status error.", platformCode, platformMsg);
            case dataLevelLowError:
                return new AdError(dataLevelLowError, "Upload data level is FORBIDDEN, must called 'ATSDK.setGDPRUploadDataLevel' to set the level.", platformCode, platformMsg);
            case networkError:
                return new AdError(networkError, "Network is unavailable.", platformCode, platformMsg);
            case serverError:
                return new AdError(serverError, "Server is unavailable.", platformCode, platformMsg);
            case timeOutError:
                return new AdError(timeOutError, "Ad load time out.", platformCode, platformMsg);
            case adapterNotExistError:
                return new AdError(adapterNotExistError, "Adapter does not exist.", platformCode, platformMsg);
            case outOfCapError:
                return new AdError(outOfCapError, "Not satisfy the Placement's Cap configuration.", platformCode, platformMsg);
            case noADError:
                return new AdError(noADError, "Return Ad is empty.", platformCode, platformMsg);
            case placeStrategyError:
                return new AdError(placeStrategyError, "Get placement strategy error, please check your network or your appid、appkey and placementid is availiable.", platformCode, platformMsg);
            case loadingError:
                return new AdError(loadingError, "Placement's Ad is loading.", platformCode, platformMsg);
            case inPacingError:
                return new AdError(inPacingError, "Not satisfy the Placement's Placing configuration.", platformCode, platformMsg);
            case contextDestoryError:
                return new AdError(contextDestoryError, "Context or activity has been destory.", platformCode, platformMsg);
            case appIdOrPlaceIdEmpty:
                return new AdError(appIdOrPlaceIdEmpty, "AppId or PlacementId is empty.", platformCode, platformMsg);
            case formatError:
                return new AdError(formatError, "Mismatched ad placement and ad format", platformCode, platformMsg);
            case placementAdClose:
                return new AdError(placementAdClose, "Placement Ads switch is close.", platformCode, platformMsg);
            case noAdsourceConfig:
                return new AdError(noAdsourceConfig, "The placement strategy does not contain any ad sources, please check the mediation configuration in TopOn", platformCode, platformMsg);
            case adapterInnerError:
                return new AdError(adapterInnerError, "Please check if your network sdk version is correct and all the network plugin has been put in your package.", platformCode, platformMsg);
            case appKeyError:
                return new AdError(appKeyError, "Please check your appkey.", platformCode, platformMsg);
            case appIdError:
                return new AdError(appIdError, "Please check your appid.", platformCode, platformMsg);
            case placementIdError:
                return new AdError(placementIdError, "Please check your placementid.", platformCode, platformMsg);
            case noVailAdsource:
                return new AdError(noVailAdsource, "Ad sources are filtered, no ad source is currently available", platformCode, platformMsg);
            case inRequestFailPacing:
                return new AdError(inRequestFailPacing, "Not satisfy the Fail-request's Placing configuration.", platformCode, platformMsg);
            case loadFailInPacingError:
                return new AdError(loadFailInPacingError, "The placement load too frequent within the specified time period after the previous load failure.", platformCode, platformMsg);
            case loadCappingError:
                return new AdError(loadCappingError, "The placement load too many times within the specified time period.", platformCode, platformMsg);
            default:
                return new AdError(unknown, "unknown", platformCode, platformMsg);
        }
    }
}
