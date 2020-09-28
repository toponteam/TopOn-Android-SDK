package com.anythink.core.common.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.anythink.core.common.base.Const;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyOfferAd implements Parcelable {
    private String offerId; //OfferId
    private String creativeId; //Resource Id
    private String title; //Offer Title
    private String desc; //Offer Description
    private String iconUrl; //Offer icon url
    private String mainImageUrl; //Offer image url
    private String endCardImageUrl; //Offer's EndCard image url（Just use by RewardedVideo and Interstitial）
    private String adChoiceUrl; //AdSource icon url
    private String ctaText; //Click to action text
    private String videoUrl; //Video url
    private int clickType; //ClickType：1：Market，2：Browser
    private String previewUrl; //Preview Click url
    private String deeplinkUrl; //Deep Link Click url
    private String clickUrl; //Click url
    private String noticeUrl; //Impression url
    private String pkgName; //Package Name

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

    int offerType; //1:Video，2:Image

    private int clickMode; //1:Asyn-Click，0：Sync-Click

    private String banner320x50Url; //Banner urls: 320x50
    private String banner320x90Url; //Banner urls: 320x50
    private String banner300x250Url; //Banner urls: 320x50
    private String banner728x90Url; //Banner urls: 320x50

    private String tkInfoMap; //for tracking


    protected MyOfferAd(Parcel in) {
        offerId = in.readString();
        creativeId = in.readString();
        title = in.readString();
        desc = in.readString();
        iconUrl = in.readString();
        mainImageUrl = in.readString();
        endCardImageUrl = in.readString();
        adChoiceUrl = in.readString();
        ctaText = in.readString();
        videoUrl = in.readString();
        clickType = in.readInt();
        previewUrl = in.readString();
        deeplinkUrl = in.readString();
        clickUrl = in.readString();
        noticeUrl = in.readString();
        pkgName = in.readString();
        videoStartTrackUrl = in.readString();
        videoProgress25TrackUrl = in.readString();
        videoProgress50TrackUrl = in.readString();
        videoProgress75TrackUrl = in.readString();
        videoFinishTrackUrl = in.readString();
        endCardShowTrackUrl = in.readString();
        endCardCloseTrackUrl = in.readString();
        impressionTrackUrl = in.readString();
        clickTrackUrl = in.readString();
        offerCap = in.readInt();
        offerPacing = in.readLong();
        updateTime = in.readLong();
        offerType = in.readInt();
        clickMode = in.readInt();
        banner320x50Url = in.readString();
        banner320x90Url = in.readString();
        banner300x250Url = in.readString();
        banner728x90Url = in.readString();
        tkInfoMap = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(offerId);
        dest.writeString(creativeId);
        dest.writeString(title);
        dest.writeString(desc);
        dest.writeString(iconUrl);
        dest.writeString(mainImageUrl);
        dest.writeString(endCardImageUrl);
        dest.writeString(adChoiceUrl);
        dest.writeString(ctaText);
        dest.writeString(videoUrl);
        dest.writeInt(clickType);
        dest.writeString(previewUrl);
        dest.writeString(deeplinkUrl);
        dest.writeString(clickUrl);
        dest.writeString(noticeUrl);
        dest.writeString(pkgName);
        dest.writeString(videoStartTrackUrl);
        dest.writeString(videoProgress25TrackUrl);
        dest.writeString(videoProgress50TrackUrl);
        dest.writeString(videoProgress75TrackUrl);
        dest.writeString(videoFinishTrackUrl);
        dest.writeString(endCardShowTrackUrl);
        dest.writeString(endCardCloseTrackUrl);
        dest.writeString(impressionTrackUrl);
        dest.writeString(clickTrackUrl);
        dest.writeInt(offerCap);
        dest.writeLong(offerPacing);
        dest.writeLong(updateTime);
        dest.writeInt(offerType);
        dest.writeInt(clickMode);
        dest.writeString(banner320x50Url);
        dest.writeString(banner320x90Url);
        dest.writeString(banner300x250Url);
        dest.writeString(banner728x90Url);
        dest.writeString(tkInfoMap);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MyOfferAd> CREATOR = new Creator<MyOfferAd>() {
        @Override
        public MyOfferAd createFromParcel(Parcel in) {
            return new MyOfferAd(in);
        }

        @Override
        public MyOfferAd[] newArray(int size) {
            return new MyOfferAd[size];
        }
    };

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

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getCreativeId() {
        return creativeId;
    }

    public void setCreativeId(String creativeId) {
        this.creativeId = creativeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public String getEndCardImageUrl() {
        return endCardImageUrl;
    }

    public void setEndCardImageUrl(String endCardImageUrl) {
        this.endCardImageUrl = endCardImageUrl;
    }

    public String getAdChoiceUrl() {
        return adChoiceUrl;
    }

    public void setAdChoiceUrl(String adChoiceUrl) {
        this.adChoiceUrl = adChoiceUrl;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getClickType() {
        return clickType;
    }

    public void setClickType(int clickType) {
        this.clickType = clickType;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getDeeplinkUrl() {
        return deeplinkUrl;
    }

    public void setDeeplinkUrl(String deeplinkUrl) {
        this.deeplinkUrl = deeplinkUrl;
    }

    public String getClickUrl() {
        return clickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
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

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }


    public int getOfferType() {
        return offerType;
    }

    public void setOfferType(int offerType) {
        this.offerType = offerType;
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
    public List<String> getUrlList(MyOfferSetting myOfferSetting) {
        //TODO Check
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

            if (offerType == 1) {
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

    public boolean isVideo() {
        return !TextUtils.isEmpty(videoUrl);
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
