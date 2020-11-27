/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.common.entity;

import android.text.TextUtils;

import com.anythink.core.common.base.Const;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyOfferAd extends BaseAdContent<MyOfferSetting> {
    private String noticeUrl; //Impression url

    private String videoStartTrackUrl; //video play tracking url
    private String videoProgress25TrackUrl; // 25% video play tracking url
    private String videoProgress50TrackUrl;//50% video play tracking url
    private String videoProgress75TrackUrl;//75% video play tracking url
    private String videoFinishTrackUrl;//video finish tracking url
    private String endCardShowTrackUrl; //endcard show tracking url
    private String endCardCloseTrackUrl; //endcard close tracking url
    private String impressionTrackUrl; //Ad impression tracking url
    private String clickTrackUrl; //Ad click tracking url

    public int offerCap; //Max cap
    public long offerPacing; //Pacing

    private long updateTime; //Udate Time

    private int clickMode; //1:Asyn-Click，0：Sync-Click

    private String banner320x50Url; //Banner urls: 320x50
    private String banner320x90Url; //Banner urls: 320x50
    private String banner300x250Url; //Banner urls: 320x50
    private String banner728x90Url; //Banner urls: 320x50

    private String tkInfoMap; //for tracking


    public String getTkInfoMap() {
        return tkInfoMap;
    }

    public void setTkInfoMap(String tkInfoMap) {
        this.tkInfoMap = tkInfoMap;
    }

    public String getBanner320x50Url() {
        return banner320x50Url;
    }

    public void setBanner320x50Url(String banner320x50Url) {
        this.banner320x50Url = banner320x50Url;
    }

    public String getBanner320x90Url() {
        return banner320x90Url;
    }

    public void setBanner320x90Url(String banner320x90Url) {
        this.banner320x90Url = banner320x90Url;
    }

    public String getBanner300x250Url() {
        return banner300x250Url;
    }

    public void setBanner300x250Url(String banner300x250Url) {
        this.banner300x250Url = banner300x250Url;
    }

    public String getBanner728x90Url() {
        return banner728x90Url;
    }

    public void setBanner728x90Url(String banner728x90Url) {
        this.banner728x90Url = banner728x90Url;
    }

    public int getClickMode() {
        return clickMode;
    }

    public void setClickMode(int clickMode) {
        this.clickMode = clickMode;
    }


    public String getNoticeUrl() {
        return noticeUrl;
    }

    public void setNoticeUrl(String noticeUrl) {
        this.noticeUrl = noticeUrl;
    }

    public String getVideoStartTrackUrl() {
        return videoStartTrackUrl;
    }

    public void setVideoStartTrackUrl(String videoStartTrackUrl) {
        this.videoStartTrackUrl = videoStartTrackUrl;
    }

    public String getVideoProgress25TrackUrl() {
        return videoProgress25TrackUrl;
    }

    public void setVideoProgress25TrackUrl(String videoProgress25TrackUrl) {
        this.videoProgress25TrackUrl = videoProgress25TrackUrl;
    }

    public String getVideoProgress50TrackUrl() {
        return videoProgress50TrackUrl;
    }

    public void setVideoProgress50TrackUrl(String videoProgress50TrackUrl) {
        this.videoProgress50TrackUrl = videoProgress50TrackUrl;
    }

    public String getVideoProgress75TrackUrl() {
        return videoProgress75TrackUrl;
    }

    public void setVideoProgress75TrackUrl(String videoProgress75TrackUrl) {
        this.videoProgress75TrackUrl = videoProgress75TrackUrl;
    }

    public String getVideoFinishTrackUrl() {
        return videoFinishTrackUrl;
    }

    public void setVideoFinishTrackUrl(String videoFinishTrackUrl) {
        this.videoFinishTrackUrl = videoFinishTrackUrl;
    }

    public String getEndCardShowTrackUrl() {
        return endCardShowTrackUrl;
    }

    public void setEndCardShowTrackUrl(String endCardShowTrackUrl) {
        this.endCardShowTrackUrl = endCardShowTrackUrl;
    }

    public String getEndCardCloseTrackUrl() {
        return endCardCloseTrackUrl;
    }

    public void setEndCardCloseTrackUrl(String endCardCloseTrackUrl) {
        this.endCardCloseTrackUrl = endCardCloseTrackUrl;
    }

    public String getImpressionTrackUrl() {
        return impressionTrackUrl;
    }

    public void setImpressionTrackUrl(String impressionTrackUrl) {
        this.impressionTrackUrl = impressionTrackUrl;
    }

    public String getClickTrackUrl() {
        return clickTrackUrl;
    }

    public void setClickTrackUrl(String clickTrackUrl) {
        this.clickTrackUrl = clickTrackUrl;
    }

    public int getOfferCap() {
        return offerCap;
    }

    public void setOfferCap(int offerCap) {
        this.offerCap = offerCap;
    }

    public long getOfferPacing() {
        return offerPacing;
    }

    public void setOfferPacing(long offerPacing) {
        this.offerPacing = offerPacing;
    }


    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }


    public MyOfferAd() {
    }

    public boolean isExpire(MyOfferSetting myOfferSetting) {
        if (myOfferSetting == null) {
            return true;
        }

        if (System.currentTimeMillis() - updateTime > myOfferSetting.getOfferCacheTime()) {
            return true;
        }
        return false;
    }

    /**
     * Resource url set
     */
    @Override
    public List<String> getUrlList(MyOfferSetting myOfferSetting) {
        ArrayList<String> urlLists = new ArrayList<>();
        boolean isCompleteResource = true;

        if (TextUtils.equals(String.valueOf(myOfferSetting.getFormat()), Const.FORMAT.NATIVE_FORMAT)) {
            //Nothing to do
        }

        if (TextUtils.equals(String.valueOf(myOfferSetting.getFormat()), Const.FORMAT.BANNER_FORMAT)) {
            String bannerSize = myOfferSetting.getBannerSize();
            boolean isPurePicture = false;
            switch (bannerSize) {
                case MyOfferSetting.BANNER_SIZE_320x90:
                    if (!TextUtils.isEmpty(banner320x90Url)) {
                        urlLists.add(banner320x90Url);
                        isPurePicture = true;
                    } else if (!TextUtils.isEmpty(endCardImageUrl)) {
                        urlLists.add(endCardImageUrl);
                    } else {
                        isCompleteResource = false;
                    }
                    break;

                case MyOfferSetting.BANNER_SIZE_300x250:
                    if (!TextUtils.isEmpty(banner300x250Url)) {
                        urlLists.add(banner300x250Url);
                        isPurePicture = true;
                    } else if (!TextUtils.isEmpty(endCardImageUrl)) {
                        urlLists.add(endCardImageUrl);
                    } else {
                        isCompleteResource = false;
                    }
                    break;

                case MyOfferSetting.BANNER_SIZE_728x90:
                    if (!TextUtils.isEmpty(banner728x90Url)) {
                        urlLists.add(banner728x90Url);
                        isPurePicture = true;
                    } else if (!TextUtils.isEmpty(endCardImageUrl)) {
                        urlLists.add(endCardImageUrl);
                    } else {
                        isCompleteResource = false;
                    }
                    break;

                case MyOfferSetting.BANNER_SIZE_320x50:
                default:
                    if (!TextUtils.isEmpty(banner320x50Url)) {
                        isPurePicture = true;
                        urlLists.add(banner320x50Url);
                    }
                    break;
            }

            if (!isPurePicture) {// assemble banner
                if (!TextUtils.isEmpty(iconUrl)) {
                    urlLists.add(iconUrl);
                } else {
                    isCompleteResource = false;
                }
            }

            if (!TextUtils.isEmpty(adChoiceUrl)) {
                urlLists.add(adChoiceUrl);
            }

        }

        if (TextUtils.equals(String.valueOf(myOfferSetting.getFormat()), Const.FORMAT.REWARDEDVIDEO_FORMAT)) {
            if (!TextUtils.isEmpty(iconUrl)) {
                urlLists.add(iconUrl);
            } else {
                isCompleteResource = false;
            }

            if (!TextUtils.isEmpty(adChoiceUrl)) {
                urlLists.add(adChoiceUrl);
            }

            if (!TextUtils.isEmpty(endCardImageUrl)) {
                urlLists.add(endCardImageUrl);
            } else {
                isCompleteResource = false;
            }

            if (!TextUtils.isEmpty(videoUrl)) {
                urlLists.add(videoUrl);
            } else {
                isCompleteResource = false;
            }
        }

        if (TextUtils.equals(myOfferSetting.getFormat() + "", Const.FORMAT.INTERSTITIAL_FORMAT)) {
            if (!TextUtils.isEmpty(iconUrl)) {
                urlLists.add(iconUrl);
            } else {
                isCompleteResource = false;
            }

            if (!TextUtils.isEmpty(adChoiceUrl)) {
                urlLists.add(adChoiceUrl);
            }

            if (!TextUtils.isEmpty(endCardImageUrl)) {
                urlLists.add(endCardImageUrl);
            } else {
                isCompleteResource = false;
            }

            if (resourceType == 1) {
                if (!TextUtils.isEmpty(videoUrl)) {
                    urlLists.add(videoUrl);
                } else {
                    isCompleteResource = false;
                }
            }

        }


        if (TextUtils.equals(String.valueOf(myOfferSetting.getFormat()), Const.FORMAT.SPLASH_FORMAT)) {
            if (!TextUtils.isEmpty(adChoiceUrl)) {
                urlLists.add(adChoiceUrl);
            }

            if (!TextUtils.isEmpty(endCardImageUrl)) {
                urlLists.add(endCardImageUrl);
            } else {
                isCompleteResource = false;
            }

        }

        if (isCompleteResource) {
            return urlLists;
        } else {
            return null;
        }

    }

    @Override
    public int getOfferSourceType() {
        return MYOFFER_TYPE;
    }

    /**
     * Replace String in url
     *
     * @param url
     * @return
     */
    public String handleTKUrlReplace(String url) {
        try {
            JSONObject tkInfoObject = new JSONObject(tkInfoMap);
            if (tkInfoObject == null) {
                return url;
            }
            Iterator<String> keyIterator = tkInfoObject.keys();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                url = url.replaceAll("\\{" + key + "\\}", tkInfoObject.optString(key));
            }
            return url;
        } catch (Throwable e) {

        }
        return url;

    }

}
