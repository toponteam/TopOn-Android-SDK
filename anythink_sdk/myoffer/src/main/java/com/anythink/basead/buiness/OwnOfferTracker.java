/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.buiness;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.anythink.basead.entity.AdClickRecord;
import com.anythink.basead.entity.ConversionRecord;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.entity.VideoViewRecord;
import com.anythink.basead.net.OwnOfferNoticeUrlLoader;
import com.anythink.basead.net.OwnOfferTkLoader;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.common.entity.OwnBaseAdTrackObject;
import com.anythink.core.common.utils.CommonUtil;

import org.json.JSONObject;

import java.util.Map;

public class OwnOfferTracker {
    protected static void sendAdTracking(final int tkType, final OwnBaseAdContent baseAdContent, @NonNull final UserOperateRecord userOperateRecord) {
        OwnBaseAdTrackObject adxTrackObject = baseAdContent.getTrackObject();
        String replaceJSONString = adxTrackObject.getReplaceJSONString();

        Map<String, Object> replaceMap = CommonUtil.jsonObjectToMap(replaceJSONString);

        sendAdNoticeUrl(tkType, baseAdContent, adxTrackObject, replaceMap, userOperateRecord);

        sendAdTopOnTracking(tkType, userOperateRecord, baseAdContent, adxTrackObject, replaceMap);
    }

    private static void sendAdNoticeUrl(int tkType, OwnBaseAdContent baseAdContent, OwnBaseAdTrackObject baseTrackObject, Map<String, Object> replaceMap, @NonNull final UserOperateRecord userOperateRecord) {

        String[] urls = null;
        try {
            switch (tkType) {
                case OfferAdFunctionUtil.VIDEO_START_TYPE:
                    urls = baseTrackObject.getVideoStartUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_PROGRESS25_TYPE:
                    urls = baseTrackObject.getVideoProgress25Urls();
                    break;
                case OfferAdFunctionUtil.VIDEO_PROGRESS50_TYPE:
                    urls = baseTrackObject.getVideoProgress50Urls();
                    break;
                case OfferAdFunctionUtil.VIDEO_PROGRESS75_TYPE:
                    urls = baseTrackObject.getVideoProgress75Urls();
                    break;
                case OfferAdFunctionUtil.VIDEO_FINISH_TYPE:
                    urls = baseTrackObject.getVideoProgress100Urls();
                    break;
                case OfferAdFunctionUtil.ENDCARD_SHOW_TYPE:
                    urls = baseTrackObject.getEndcardShowUrls();
                    break;
                case OfferAdFunctionUtil.ENDCARD_CLOSE_TYPE:
                    urls = baseTrackObject.getEndcardCloseUrls();
                    break;
                case OfferAdFunctionUtil.IMPRESSION_TYPE:
                    urls = baseTrackObject.getImpressionUrls();
                    break;
                case OfferAdFunctionUtil.CLICK_TYPE:
                    urls = baseTrackObject.getClickUrls();
                    break;
                case OfferAdFunctionUtil.NOTICE_WIN_TYPE:
                    urls = baseTrackObject.getNoticeWinUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_PAUSE_TYPE:
                    urls = baseTrackObject.getVideoPauseUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_MUTE_TYPE:
                    urls = baseTrackObject.getVideoMuteUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_NO_MUTE_TYPE:
                    urls = baseTrackObject.getVideoVoiceUrls();
                    break;
                case OfferAdFunctionUtil.APK_DOWNLOAD_START_TYPE:
                    urls = baseTrackObject.getApkDownloadStartUrls();
                    break;
                case OfferAdFunctionUtil.APK_DOWNLOAD_END_TYPE:
                    urls = baseTrackObject.getApkDownloadEndUrls();
                    break;
                case OfferAdFunctionUtil.APK_INSTALL_FINISH_TYPE:
                    urls = baseTrackObject.getApkFinishInstallUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_CLICK_TYPE:
                    urls = baseTrackObject.getVideoClickUrls();
                    break;

                case OfferAdFunctionUtil.VIDEO_RESUME_TYPE:
                    urls = baseTrackObject.getVideoResumeUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_SKIP_TYPE:
                    urls = baseTrackObject.getVideoSkipUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_ERROR_TYPE:
                    urls = baseTrackObject.getVideoPlayFailUrls();
                    break;
                case OfferAdFunctionUtil.APK_INSTALL_START_TYPE:
                    urls = baseTrackObject.getApkStartInstallUrls();
                    break;
                case OfferAdFunctionUtil.APP_START_ACTIVE_TYPE:
                    urls = baseTrackObject.getDeeplinkStartUrls();
                    break;
                case OfferAdFunctionUtil.APP_ACTIVE_SUCCESS_TYPE:
                    urls = baseTrackObject.getDeeplinkSuccessUrls();
                    break;
                case OfferAdFunctionUtil.APP_HAS_INSTALL_TYPE:
                    urls = baseTrackObject.getAppHasInstallsUrls();
                    break;
                case OfferAdFunctionUtil.APP_NO_INSTALL_TYPE:
                    urls = baseTrackObject.getAppNoInstallUrls();
                    break;
                case OfferAdFunctionUtil.APP_UNKOWN_TYPE:
                    urls = baseTrackObject.getAppUnknowUrls();
                    break;

                /**Add by v5.7.7**/
                case OfferAdFunctionUtil.APP_DEEPLINK_INSTALLED_FAIL_TYPE:
                    urls = baseTrackObject.getDeeplinkInstallFailUrls();
                    break;
                case OfferAdFunctionUtil.APP_DEEPLINK_UNINSTALLED_FAIL_TYPE:
                    urls = baseTrackObject.getDeeplinkUninstallFailUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_DOWNLOAD_SUCCESS_TYPE:
                    urls = baseTrackObject.getVideoDownloadSuccessUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_REWARDED_TYPE:
                    urls = baseTrackObject.getRewardUrls();
                    break;
                case OfferAdFunctionUtil.VIDEO_DIRECT_PROGRESS_TYPE:
                    VideoViewRecord videoViewRecord = userOperateRecord.videoViewRecord;
                    Map<Integer, String[]> directProgressUrlMap = baseTrackObject.getVideoDirectProgressUrls();
                    if (videoViewRecord != null && directProgressUrlMap != null) {
                        urls = directProgressUrlMap.get(videoViewRecord.videoDirectTrackingProgress);
                    }
                    break;
            }

            if (urls != null) {
                long trackingTime = System.currentTimeMillis();
                for (String url : urls) {
                    String finalTrackUrl = url;

                    finalTrackUrl = replaceTrackUrlInfo(finalTrackUrl, userOperateRecord, trackingTime);

                    new OwnOfferNoticeUrlLoader(tkType, finalTrackUrl, baseAdContent, replaceMap).start(0, null);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static void sendAdTopOnTracking(int tkType, @NonNull final UserOperateRecord userOperateRecord, OwnBaseAdContent baseAdContent, OwnBaseAdTrackObject baseTrackObject, Map<String, Object> replaceMap) {
        String trackUrlJSONString = "";
        switch (tkType) {
            case OfferAdFunctionUtil.VIDEO_START_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoStartJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_PROGRESS25_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoProgress25JSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_PROGRESS50_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoProgress50JSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_PROGRESS75_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoProgress75JSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_FINISH_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoProgress100JSONString();
                break;
            case OfferAdFunctionUtil.ENDCARD_SHOW_TYPE:
                trackUrlJSONString = baseTrackObject.getTpEndcardShowJSONString();
                break;
            case OfferAdFunctionUtil.ENDCARD_CLOSE_TYPE:
                trackUrlJSONString = baseTrackObject.getTpEndcardCloseJSONString();
                break;
            case OfferAdFunctionUtil.IMPRESSION_TYPE:
                trackUrlJSONString = baseTrackObject.getTpImpressionJSONString();
                break;
            case OfferAdFunctionUtil.CLICK_TYPE:
                trackUrlJSONString = baseTrackObject.getTpClickJSONString();
                break;
            case OfferAdFunctionUtil.NOTICE_WIN_TYPE:
                trackUrlJSONString = baseTrackObject.getTpNoticeWinJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_PAUSE_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoPauseJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_MUTE_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoMuteJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_NO_MUTE_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoVoiceJSONStrings();
                break;
            case OfferAdFunctionUtil.APK_DOWNLOAD_START_TYPE:
                trackUrlJSONString = baseTrackObject.getTpApkDownloadStartJSONString();
                break;
            case OfferAdFunctionUtil.APK_DOWNLOAD_END_TYPE:
                trackUrlJSONString = baseTrackObject.getTpApkDownloadEndJSONString();
                break;
            case OfferAdFunctionUtil.APK_INSTALL_FINISH_TYPE:
                trackUrlJSONString = baseTrackObject.getTpApkFinishInstallJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_CLICK_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoClickJSONString();
                break;

            case OfferAdFunctionUtil.VIDEO_RESUME_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoResumeJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_SKIP_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoSkipJSONString();
                break;
            case OfferAdFunctionUtil.VIDEO_ERROR_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoPlayFailJSONString();
                break;
            case OfferAdFunctionUtil.APK_INSTALL_START_TYPE:
                trackUrlJSONString = baseTrackObject.getTpApkStartInstallJSONString();
                break;
            case OfferAdFunctionUtil.APP_START_ACTIVE_TYPE:
                trackUrlJSONString = baseTrackObject.getTpDeeplinkStartJSONString();
                break;
            case OfferAdFunctionUtil.APP_ACTIVE_SUCCESS_TYPE:
                trackUrlJSONString = baseTrackObject.getTpDeeplinkSuccessJSONString();
                break;
            case OfferAdFunctionUtil.APP_HAS_INSTALL_TYPE:
                trackUrlJSONString = baseTrackObject.getTpAppHasInstallsJSONString();
                break;
            case OfferAdFunctionUtil.APP_NO_INSTALL_TYPE:
                trackUrlJSONString = baseTrackObject.getTpAppNoInstallJSONString();
                break;
            case OfferAdFunctionUtil.APP_UNKOWN_TYPE:
                trackUrlJSONString = baseTrackObject.getTpAppUnknowJSONString();
                break;
            /**Add by v5.7.7**/
            case OfferAdFunctionUtil.APP_DEEPLINK_INSTALLED_FAIL_TYPE:
                trackUrlJSONString = baseTrackObject.getTpDeeplinkInstallFailUrls();
                break;
            case OfferAdFunctionUtil.APP_DEEPLINK_UNINSTALLED_FAIL_TYPE:
                trackUrlJSONString = baseTrackObject.getTpDeeplinkUninstallFailUrls();
                break;
            case OfferAdFunctionUtil.VIDEO_DOWNLOAD_SUCCESS_TYPE:
                trackUrlJSONString = baseTrackObject.getTpVideoDownloadSuccessUrls();
                break;
            case OfferAdFunctionUtil.VIDEO_REWARDED_TYPE:
                trackUrlJSONString = baseTrackObject.getTpRewardUrls();
                break;
            case OfferAdFunctionUtil.VIDEO_DIRECT_PROGRESS_TYPE:
                break;
        }

        if (checkTrackingInfoIsEmpty(trackUrlJSONString)) {
            return;
        }

        OwnOfferTkLoader ownOfferTkLoader = new OwnOfferTkLoader(tkType, baseAdContent, trackUrlJSONString, replaceMap);
        ownOfferTkLoader.setScenario(userOperateRecord.scenario);
        ownOfferTkLoader.start(0, null);
    }

    protected static String replaceTrackUrlInfo(String trackUrl, UserOperateRecord userOperateRecord, long currentMillTime) {
        if (TextUtils.isEmpty(trackUrl)) {
            return "";
        }

        String orginUrl = trackUrl;
        if (userOperateRecord.adClickRecord != null) {
            orginUrl = replaceClickTrackingInfo(orginUrl, userOperateRecord.adClickRecord);
        }

        if (userOperateRecord.videoViewRecord != null) {
            orginUrl = replaceVideoTrackingInfo(orginUrl, userOperateRecord.videoViewRecord);
        }

        if (userOperateRecord.conversionRecord != null) {
            orginUrl = replaceConversionTrackingInfo(orginUrl, userOperateRecord.conversionRecord);
        }

        long currentTime = currentMillTime / 1000;

        //Replace the tracking time
        orginUrl = orginUrl.replaceAll("\\{__REQ_WIDTH__\\}", userOperateRecord.requestWidth == 0 ? "__REQ_WIDTH__" : userOperateRecord.requestWidth + "")
                .replaceAll("\\{__REQ_HEIGHT__\\}", userOperateRecord.requestHeight == 0 ? "__REQ_HEIGHT__" : userOperateRecord.requestHeight + "")
                .replaceAll("\\{__WIDTH__\\}", userOperateRecord.realWidth + "")
                .replaceAll("\\{__HEIGHT__\\}", userOperateRecord.realHeight + "")
                .replaceAll("\\{__TS__\\}", currentTime + "")
                .replaceAll("\\{__TS_MSEC__\\}", currentMillTime + "")
                .replaceAll("\\{__END_TS__\\}", currentTime + "")
                .replaceAll("\\{__END_TS_MSEC__\\}", currentMillTime + "")
                .replaceAll("\\{__PLAY_SEC__\\}",  "0");

        /**
         * replace {}
         */
        orginUrl = orginUrl.replaceAll("\\{", "").replaceAll("\\}", "");
        return orginUrl;
    }

    private static String replaceVideoTrackingInfo(String trackingUrl, VideoViewRecord videoViewRecord) {
        String finishReplaceUrl = trackingUrl.replaceAll("\\{__VIDEO_TIME__\\}", videoViewRecord.videoLength + "")
                .replaceAll("\\{__BEGIN_TIME__\\}", videoViewRecord.videoStartTime + "")
                .replaceAll("\\{__END_TIME__\\}", videoViewRecord.videoEndTime + "")
                .replaceAll("\\{__PLAY_FIRST_FRAME__\\}", videoViewRecord.isVideoPlayInStart + "")
                .replaceAll("\\{__PLAY_LAST_FRAME__\\}", videoViewRecord.isVideoPlayInEnd + "")
                .replaceAll("\\{__SCENE__\\}", videoViewRecord.viodePlayScence + "")
                .replaceAll("\\{__TYPE__\\}", videoViewRecord.videoPlayType + "")
                .replaceAll("\\{__BEHAVIOR__\\}", videoViewRecord.videoPlayBehavior + "")
                .replaceAll("\\{__STATUS__\\}", videoViewRecord.videoPlayStatus + "")
                .replaceAll("\\{__PLAY_SEC__\\}", videoViewRecord.videoCurrentMillPosition + "")
                .replaceAll("\\{__TS__\\}", videoViewRecord.videoStartUTCMillTime / 1000 + "")
                .replaceAll("\\{__TS_MSEC__\\}", videoViewRecord.videoStartUTCMillTime + "")
                .replaceAll("\\{__END_TS__\\}", videoViewRecord.videoEndUTCMillTime / 1000 + "")
                .replaceAll("\\{__END_TS_MSEC__\\}", videoViewRecord.videoEndUTCMillTime + "")
                .replaceAll("\\{__PLAY_SEC__\\}", videoViewRecord.videoCurrentMillPosition / 1000 + "")
                .replaceAll("\\{__PLAY_MSEC__\\}", videoViewRecord.videoCurrentMillPosition + "");
        return finishReplaceUrl;
    }


    private static String replaceConversionTrackingInfo(String trackingUrl, ConversionRecord conversionRecord) {
        String finishReplaceUrl = trackingUrl.replaceAll("\\{__CLICK_ID__\\}", conversionRecord.clickId == null ? "" : conversionRecord.clickId);
        return finishReplaceUrl;
    }

    private static String replaceClickTrackingInfo(String trackingUrl, AdClickRecord adClickRecord) {
        String finishReplaceUrl = trackingUrl.replaceAll("\\{__DOWN_X__\\}", adClickRecord.clickDownX + "")
                .replaceAll("\\{__DOWN_Y__\\}", adClickRecord.clickDownY + "")
                .replaceAll("\\{__UP_X__\\}", adClickRecord.clickUpX + "")
                .replaceAll("\\{__UP_Y__\\}", adClickRecord.clickUpY + "")
                .replaceAll("\\{__RE_DOWN_X__\\}", adClickRecord.clickRelateDownX + "")
                .replaceAll("\\{__RE_DOWN_Y__\\}", adClickRecord.clickRelateDownY + "")
                .replaceAll("\\{__RE_UP_X__\\}", adClickRecord.clickRelateUpX + "")
                .replaceAll("\\{__RE_UP_Y__\\}", adClickRecord.clickRelateUpY + "");

        return finishReplaceUrl;
    }

    private static boolean checkTrackingInfoIsEmpty(String trackString) {
        if (TextUtils.isEmpty(trackString)) {
            return true;
        }
        try {
            JSONObject jsonObject = new JSONObject(trackString);
            if (jsonObject.length() > 0) {
                return false;
            }
        } catch (Throwable e) {

        }
        return true;
    }
}
