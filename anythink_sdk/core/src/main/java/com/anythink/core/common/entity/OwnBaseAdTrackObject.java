/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import com.anythink.core.common.utils.CommonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class OwnBaseAdTrackObject implements Serializable {
    String replaceJSONString;
    String[] noticeWinUrls;
    String[] impressionUrls;
    String[] clickUrls;
    String[] videoStartUrls;
    String[] videoProgress25Urls;
    String[] videoProgress50Urls;
    String[] videoProgress75Urls;
    String[] videoProgress100Urls;
    String[] videoPauseUrls;
    String[] videoClickUrls;
    String[] videoMuteUrls;
    String[] videoVoiceUrls;
    String[] endcardShowUrls;
    String[] endcardCloseUrls;
    String[] apkDownloadStartUrls;
    String[] apkDownloadEndUrls;
    String[] apkFinishInstallUrls;

    /**
     * 5.7.3 Add
     */
    String[] videoResumeUrls;
    String[] videoSkipUrls;
    String[] videoPlayFailUrls;
    String[] apkStartInstallUrls;
    String[] deeplinkStartUrls;
    String[] deeplinkSuccessUrls;
    String[] appHasInstallsUrls;
    String[] appNoInstallUrls;
    String[] appUnknowUrls;

    /**
     * 5.7.7 Add
     */
    String[] deeplinkUninstallFailUrls;
    String[] deeplinkInstallFailUrls;
    String[] videoDownloadSuccessUrls;
    String[] rewardUrls;
    Map<Integer, String[]> videoDirectProgressUrls;


    String tpNoticeWinJSONString;
    String tpImpressionJSONString;
    String tpClickJSONString;
    String tpVideoStartJSONString;
    String tpVideoProgress25JSONString;
    String tpVideoProgress50JSONString;
    String tpVideoProgress75JSONString;
    String tpVideoProgress100JSONString;
    String tpVideoPauseJSONString;
    String tpVideoClickJSONString;
    String tpVideoMuteJSONString;
    String tpVideoVoiceJSONStrings;
    String tpEndcardShowJSONString;
    String tpEndcardCloseJSONString;
    String tpApkDownloadStartJSONString;
    String tpApkDownloadEndJSONString;
    String tpApkFinishInstallJSONString;

    /**
     * 5.7.3 Add
     */
    String tpVideoResumeJSONString;
    String tpVideoSkipJSONString;
    String tpVideoPlayFailJSONString;
    String tpApkStartInstallJSONString;
    String tpDeeplinkStartJSONString;
    String tpDeeplinkSuccessJSONString;
    String tpAppHasInstallsJSONString;
    String tpAppNoInstallJSONString;
    String tpAppUnknowJSONString;

    /**
     * 5.7.7 Add
     */
    String tpDeeplinkUninstallFailUrls;
    String tpDeeplinkInstallFailUrls;
    String tpVideoDownloadSuccessUrls;
    String tpRewardUrls;

    public final static OwnBaseAdTrackObject parseAdxTrackObject(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            OwnBaseAdTrackObject trackObject = new OwnBaseAdTrackObject();
            trackObject.replaceJSONString = jsonObject.optString("ks");
            trackObject.noticeWinUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("nurl"));
            trackObject.impressionUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("imp"));
            trackObject.clickUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("click"));
            trackObject.videoStartUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vstart"));
            trackObject.videoProgress25Urls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("v25"));
            trackObject.videoProgress50Urls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("v50"));
            trackObject.videoProgress75Urls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("v75"));
            trackObject.videoProgress100Urls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("v100"));
            trackObject.videoPauseUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vpaused"));
            trackObject.videoClickUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vclick"));
            trackObject.videoMuteUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vmute"));
            trackObject.videoVoiceUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vunmute"));
            trackObject.endcardShowUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("ec_show"));
            trackObject.endcardCloseUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("ec_close"));
            trackObject.apkDownloadStartUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("apk_dl_star"));
            trackObject.apkDownloadEndUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("apk_dl_end"));
            trackObject.apkFinishInstallUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("apk_install"));

            /**
             * v5.7.3
             */
            trackObject.videoResumeUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vresumed"));
            trackObject.videoSkipUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vskip"));
            trackObject.videoPlayFailUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vfail"));
            trackObject.apkStartInstallUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("apk_start_install"));
            trackObject.deeplinkStartUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("dp_start"));
            trackObject.deeplinkSuccessUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("dp_succ"));
            trackObject.appHasInstallsUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("app_install"));
            trackObject.appNoInstallUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("app_uninstall"));
            trackObject.appUnknowUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("app_unknow"));

            /**
             * v5.7.7
             */
            trackObject.deeplinkInstallFailUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("dp_inst_fail"));
            trackObject.deeplinkUninstallFailUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("dp_uninst_fail"));
            trackObject.videoDownloadSuccessUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vd_succ"));
            trackObject.rewardUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("vrewarded"));
            JSONArray progressArray = jsonObject.optJSONArray("v_p_tracking");
            if (progressArray != null) {
                trackObject.videoDirectProgressUrls = new HashMap<>();
                for (int i = 0; i < progressArray.length(); i++) {
                    JSONObject progressObject = progressArray.optJSONObject(i);
                    int videoPosition = progressObject.optInt("play_sec");
                    JSONArray progressUrlArray = progressObject.optJSONArray("list");
                    String[] progressUrls = CommonUtil.jsonArrayToStringArray(progressUrlArray);
                    trackObject.videoDirectProgressUrls.put(videoPosition, progressUrls);
                }
            }

            trackObject.tpNoticeWinJSONString = jsonObject.optString("tp_nurl");
            trackObject.tpImpressionJSONString = jsonObject.optString("tp_imp");
            trackObject.tpClickJSONString = jsonObject.optString("tp_click");
            trackObject.tpVideoStartJSONString = jsonObject.optString("tp_vstart");
            trackObject.tpVideoProgress25JSONString = jsonObject.optString("tp_v25");
            trackObject.tpVideoProgress50JSONString = jsonObject.optString("tp_v50");
            trackObject.tpVideoProgress75JSONString = jsonObject.optString("tp_v75");
            trackObject.tpVideoProgress100JSONString = jsonObject.optString("tp_v100");
            trackObject.tpVideoPauseJSONString = jsonObject.optString("tp_vpaused");
            trackObject.tpVideoClickJSONString = jsonObject.optString("tp_vclick");
            trackObject.tpVideoMuteJSONString = jsonObject.optString("tp_vmute");
            trackObject.tpVideoVoiceJSONStrings = jsonObject.optString("tp_vunmute");
            trackObject.tpEndcardShowJSONString = jsonObject.optString("tp_ec_show");
            trackObject.tpEndcardCloseJSONString = jsonObject.optString("tp_ec_close");
            trackObject.tpApkDownloadStartJSONString = jsonObject.optString("tp_apk_dl_star");
            trackObject.tpApkDownloadEndJSONString = jsonObject.optString("tp_apk_dl_end");
            trackObject.tpApkFinishInstallJSONString = jsonObject.optString("tp_apk_install");

            /**
             * v5.7.3
             */
            trackObject.tpVideoResumeJSONString = jsonObject.optString("tp_vresumed");
            trackObject.tpVideoSkipJSONString = jsonObject.optString("tp_vskip");
            trackObject.tpVideoPlayFailJSONString = jsonObject.optString("tp_vfail");
            trackObject.tpApkStartInstallJSONString = jsonObject.optString("tp_apk_start_install");
            trackObject.tpDeeplinkStartJSONString = jsonObject.optString("tp_dp_start");
            trackObject.tpDeeplinkSuccessJSONString = jsonObject.optString("tp_dp_succ");
            trackObject.tpAppHasInstallsJSONString = jsonObject.optString("tp_app_install");
            trackObject.tpAppNoInstallJSONString = jsonObject.optString("tp_app_uninstall");
            trackObject.tpAppUnknowJSONString = jsonObject.optString("tp_app_unknow");

            /**
             * v5.7.7
             */
            trackObject.tpDeeplinkInstallFailUrls = jsonObject.optString("tp_dp_inst_fail");
            trackObject.tpDeeplinkUninstallFailUrls = jsonObject.optString("tp_dp_uninst_fail");
            trackObject.tpVideoDownloadSuccessUrls = jsonObject.optString("tp_vd_succ");
            trackObject.tpRewardUrls = jsonObject.optString("tp_vrewarded");

            return trackObject;

        } catch (Throwable e) {

        }
        return null;
    }



    public String getReplaceJSONString() {
        return replaceJSONString;
    }

    public String[] getNoticeWinUrls() {
        return noticeWinUrls;
    }

    public String[] getImpressionUrls() {
        return impressionUrls;
    }

    public String[] getClickUrls() {
        return clickUrls;
    }

    public String[] getVideoStartUrls() {
        return videoStartUrls;
    }

    public String[] getVideoProgress25Urls() {
        return videoProgress25Urls;
    }

    public String[] getVideoProgress50Urls() {
        return videoProgress50Urls;
    }

    public String[] getVideoProgress75Urls() {
        return videoProgress75Urls;
    }

    public String[] getVideoProgress100Urls() {
        return videoProgress100Urls;
    }

    public String[] getVideoPauseUrls() {
        return videoPauseUrls;
    }

    public String[] getVideoClickUrls() {
        return videoClickUrls;
    }

    public String[] getVideoMuteUrls() {
        return videoMuteUrls;
    }

    public String[] getVideoVoiceUrls() {
        return videoVoiceUrls;
    }

    public String[] getEndcardShowUrls() {
        return endcardShowUrls;
    }

    public String[] getEndcardCloseUrls() {
        return endcardCloseUrls;
    }

    public String[] getApkDownloadStartUrls() {
        return apkDownloadStartUrls;
    }

    public String[] getApkDownloadEndUrls() {
        return apkDownloadEndUrls;
    }

    public String[] getApkFinishInstallUrls() {
        return apkFinishInstallUrls;
    }

    public String[] getDeeplinkUninstallFailUrls() {
        return deeplinkUninstallFailUrls;
    }

    public String[] getDeeplinkInstallFailUrls() {
        return deeplinkInstallFailUrls;
    }

    public String[] getVideoDownloadSuccessUrls() {
        return videoDownloadSuccessUrls;
    }

    public String[] getRewardUrls() {
        return rewardUrls;
    }

    public Map<Integer, String[]> getVideoDirectProgressUrls() {
        return videoDirectProgressUrls;
    }

    public String getTpNoticeWinJSONString() {
        return tpNoticeWinJSONString;
    }

    public String getTpImpressionJSONString() {
        return tpImpressionJSONString;
    }

    public String getTpClickJSONString() {
        return tpClickJSONString;
    }

    public String getTpVideoStartJSONString() {
        return tpVideoStartJSONString;
    }

    public String getTpVideoProgress25JSONString() {
        return tpVideoProgress25JSONString;
    }

    public String getTpVideoProgress50JSONString() {
        return tpVideoProgress50JSONString;
    }

    public String getTpVideoProgress75JSONString() {
        return tpVideoProgress75JSONString;
    }

    public String getTpVideoProgress100JSONString() {
        return tpVideoProgress100JSONString;
    }

    public String getTpVideoPauseJSONString() {
        return tpVideoPauseJSONString;
    }

    public String getTpVideoClickJSONString() {
        return tpVideoClickJSONString;
    }

    public String getTpVideoMuteJSONString() {
        return tpVideoMuteJSONString;
    }

    public String getTpVideoVoiceJSONStrings() {
        return tpVideoVoiceJSONStrings;
    }

    public String getTpEndcardShowJSONString() {
        return tpEndcardShowJSONString;
    }

    public String getTpEndcardCloseJSONString() {
        return tpEndcardCloseJSONString;
    }

    public String getTpApkDownloadStartJSONString() {
        return tpApkDownloadStartJSONString;
    }

    public String getTpApkDownloadEndJSONString() {
        return tpApkDownloadEndJSONString;
    }

    public String getTpApkFinishInstallJSONString() {
        return tpApkFinishInstallJSONString;
    }

    public String[] getVideoResumeUrls() {
        return videoResumeUrls;
    }

    public String[] getVideoSkipUrls() {
        return videoSkipUrls;
    }

    public String[] getVideoPlayFailUrls() {
        return videoPlayFailUrls;
    }

    public String[] getApkStartInstallUrls() {
        return apkStartInstallUrls;
    }


    public String[] getDeeplinkStartUrls() {
        return deeplinkStartUrls;
    }

    public String[] getDeeplinkSuccessUrls() {
        return deeplinkSuccessUrls;
    }

    public String[] getAppHasInstallsUrls() {
        return appHasInstallsUrls;
    }

    public String[] getAppNoInstallUrls() {
        return appNoInstallUrls;
    }

    public String[] getAppUnknowUrls() {
        return appUnknowUrls;
    }




    public String getTpVideoResumeJSONString() {
        return tpVideoResumeJSONString;
    }

    public String getTpVideoSkipJSONString() {
        return tpVideoSkipJSONString;
    }

    public String getTpVideoPlayFailJSONString() {
        return tpVideoPlayFailJSONString;
    }

    public String getTpApkStartInstallJSONString() {
        return tpApkStartInstallJSONString;
    }

    public String getTpDeeplinkStartJSONString() {
        return tpDeeplinkStartJSONString;
    }

    public String getTpDeeplinkSuccessJSONString() {
        return tpDeeplinkSuccessJSONString;
    }

    public String getTpAppHasInstallsJSONString() {
        return tpAppHasInstallsJSONString;
    }

    public String getTpAppNoInstallJSONString() {
        return tpAppNoInstallJSONString;
    }

    public String getTpAppUnknowJSONString() {
        return tpAppUnknowJSONString;
    }

    public String getTpDeeplinkUninstallFailUrls() {
        return tpDeeplinkUninstallFailUrls;
    }

    public String getTpDeeplinkInstallFailUrls() {
        return tpDeeplinkInstallFailUrls;
    }

    public String getTpVideoDownloadSuccessUrls() {
        return tpVideoDownloadSuccessUrls;
    }

    public String getTpRewardUrls() {
        return tpRewardUrls;
    }
}
