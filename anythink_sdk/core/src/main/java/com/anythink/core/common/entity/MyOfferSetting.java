package com.anythink.core.common.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MyOfferSetting implements Parcelable {

    private int format;//ad type： 0 =Native，1= RV，2=banner ，3=inter ，4=splash
    private int videoClick;//Video click handl, 0: No response,  1: Open market or download apk
    private int showBannerTime;//Banner Show Time, -1: User click to show, -2: No show,  0: Show banner when video start
    private int endCardClickArea;//EndCard Click area, 0: Fullscreen (Default）, 1: CTA button, 2: Banner Area
    private int videoMute;//Sound mode, 0: Mute， 1：System volume
    private int showCloseTime;//Close show time
    private int offerTimeout;//MyOffer resource download timeout
    private long offerCacheTime;//MyOffer Cache time, Default: 604800ms

    private int apkDownloadConfirm;//1:true, 0:false
    private int canSplashSkip;//0:true,1:false
    private long splashCountdownTime; //Splash Countdown Time
    private int splashOrientation; //1:portrait, 2:landscape

    private String bannerSize; //320x50,320x90,300x250,728x90
    private int isShowCloseButton; //1:true, 2:false

    public static final String BANNER_SIZE_320x50 = "320x50";
    public static final String BANNER_SIZE_320x90 = "320x90";
    public static final String BANNER_SIZE_300x250 = "300x250";
    public static final String BANNER_SIZE_728x90 = "728x90";

    public MyOfferSetting() {

    }


    protected MyOfferSetting(Parcel in) {
        format = in.readInt();
        videoClick = in.readInt();
        showBannerTime = in.readInt();
        endCardClickArea = in.readInt();
        videoMute = in.readInt();
        showCloseTime = in.readInt();
        offerTimeout = in.readInt();
        offerCacheTime = in.readLong();
        apkDownloadConfirm = in.readInt();
        canSplashSkip = in.readInt();
        splashCountdownTime = in.readLong();
        splashOrientation = in.readInt();
        bannerSize = in.readString();
        isShowCloseButton = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(format);
        dest.writeInt(videoClick);
        dest.writeInt(showBannerTime);
        dest.writeInt(endCardClickArea);
        dest.writeInt(videoMute);
        dest.writeInt(showCloseTime);
        dest.writeInt(offerTimeout);
        dest.writeLong(offerCacheTime);
        dest.writeInt(apkDownloadConfirm);
        dest.writeInt(canSplashSkip);
        dest.writeLong(splashCountdownTime);
        dest.writeInt(splashOrientation);
        dest.writeString(bannerSize);
        dest.writeInt(isShowCloseButton);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MyOfferSetting> CREATOR = new Creator<MyOfferSetting>() {
        @Override
        public MyOfferSetting createFromParcel(Parcel in) {
            return new MyOfferSetting(in);
        }

        @Override
        public MyOfferSetting[] newArray(int size) {
            return new MyOfferSetting[size];
        }
    };

    public long getSplashCountdownTime() {
        return splashCountdownTime;
    }

    public void setSplashCountdownTime(long splashCountdownTime) {
        this.splashCountdownTime = splashCountdownTime;
    }

    public int getApkDownloadConfirm() {
        return apkDownloadConfirm;
    }

    public void setApkDownloadConfirm(int apkDownloadConfirm) {
        this.apkDownloadConfirm = apkDownloadConfirm;
    }

    public int getCanSplashSkip() {
        return canSplashSkip;
    }

    public void setCanSplashSkip(int canSplashSkip) {
        this.canSplashSkip = canSplashSkip;
    }

    public int getSplashOrientation() {
        return splashOrientation;
    }

    public void setSplashOrientation(int splashOrientation) {
        this.splashOrientation = splashOrientation;
    }

    public String getBannerSize() {
        return bannerSize;
    }

    public void setBannerSize(String bannerSize) {
        this.bannerSize = bannerSize;
    }

    public int getIsShowCloseButton() {
        return isShowCloseButton;
    }

    public void setIsShowCloseButton(int isShowCloseButton) {
        this.isShowCloseButton = isShowCloseButton;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getVideoClick() {
        return videoClick;
    }

    public void setVideoClick(int videoClick) {
        this.videoClick = videoClick;
    }

    public int getShowBannerTime() {
        return showBannerTime;
    }

    public void setShowBannerTime(int showBannerTime) {
        this.showBannerTime = showBannerTime;
    }

    public int getEndCardClickArea() {
        return endCardClickArea;
    }

    public void setEndCardClickArea(int endCardClickArea) {
        this.endCardClickArea = endCardClickArea;
    }

    public int getVideoMute() {
        return videoMute;
    }

    public void setVideoMute(int videoMute) {
        this.videoMute = videoMute;
    }

    public int getShowCloseTime() {
        return showCloseTime;
    }

    public void setShowCloseTime(int showCloseTime) {
        this.showCloseTime = showCloseTime;
    }

    public int getOfferTimeout() {
        return offerTimeout;
    }

    public void setOfferTimeout(int offerTimeout) {
        this.offerTimeout = offerTimeout;
    }

    public long getOfferCacheTime() {
        return offerCacheTime;
    }

    public void setOfferCacheTime(long offerCacheTime) {
        this.offerCacheTime = offerCacheTime;
    }

    public static MyOfferSetting parseMyOfferSetting(String json) {
        MyOfferSetting myOfferSetting = new MyOfferSetting();
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject jsonObject = new JSONObject(json);

                myOfferSetting.setFormat(jsonObject.optInt("f_t"));
                myOfferSetting.setVideoClick(jsonObject.optInt("v_c"));
                myOfferSetting.setShowBannerTime(jsonObject.optInt("s_b_t"));
                myOfferSetting.setEndCardClickArea(jsonObject.optInt("e_c_a"));
                myOfferSetting.setVideoMute(jsonObject.optInt("v_m"));
                myOfferSetting.setShowCloseTime(jsonObject.optInt("s_c_t"));
                myOfferSetting.setOfferTimeout(jsonObject.optInt("m_t"));
                myOfferSetting.setOfferCacheTime(jsonObject.optLong("o_c_t"));

                myOfferSetting.setApkDownloadConfirm(jsonObject.optInt("ak_cfm"));

                myOfferSetting.setSplashCountdownTime(jsonObject.optLong("ctdown_time"));
                myOfferSetting.setCanSplashSkip(jsonObject.optInt("sk_able"));
                myOfferSetting.setSplashOrientation(jsonObject.optInt("orient"));
                myOfferSetting.setBannerSize(jsonObject.optString("size"));
                myOfferSetting.setIsShowCloseButton(jsonObject.optInt("cl_btn"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return myOfferSetting;
    }
}
