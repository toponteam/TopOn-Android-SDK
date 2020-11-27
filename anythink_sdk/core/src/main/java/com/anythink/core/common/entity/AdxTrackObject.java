/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import com.anythink.core.common.utils.CommonUtil;

import org.json.JSONObject;

import java.io.Serializable;

public class AdxTrackObject implements Serializable {
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
    String[] apkInstallUrls;

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
    String tpApkInstallJSONString;

    public final static AdxTrackObject parseAdxTrackObject(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            AdxTrackObject trackObject = new AdxTrackObject();
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
            trackObject.apkInstallUrls = CommonUtil.jsonArrayToStringArray(jsonObject.optJSONArray("apk_install"));

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
            trackObject.tpApkInstallJSONString = jsonObject.optString("tp_apk_install");

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

    public String[] getApkInstallUrls() {
        return apkInstallUrls;
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

    public String getTpApkInstallJSONString() {
        return tpApkInstallJSONString;
    }
}
