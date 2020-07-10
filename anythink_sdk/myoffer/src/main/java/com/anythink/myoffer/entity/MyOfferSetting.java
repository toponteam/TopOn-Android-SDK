package com.anythink.myoffer.entity;

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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return myOfferSetting;
    }
}
