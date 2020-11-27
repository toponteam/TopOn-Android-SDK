/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.unitgroup.api;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.core.api.ATSDK;
import com.anythink.nativead.unitgroup.BaseNativeAd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Z on 2018/1/8.
 */

public class CustomNativeAd extends BaseNativeAd {

    static final double MIN_STAR_RATING = 0;
    static final double MAX_STAR_RATING = 5;

    public static String IS_AUTO_PLAY_KEY = "is_auto_play";
    public static String AD_REQUEST_NUM = "ad_num";

    // Basic fields
    private String mMainImageUrl;
    private String mIconImageUrl;
    private String mClickDestinationUrl;
    private String mCallToAction;
    private String mTitle;
    private String mText;
    private Double mStarRating = 0d;
    //    private String mPrivacyInformationIconClickThroughUrl;
//    private String mPrivacyInformationIconImageUrl;
    private String mVideoUrl;
    private String mAdChoiceIconUrl;
    private String mAdFrom;

    private List<String> mImageUrlList;


    // Extras
//    private final Map<String, Object> mExtras;

    public CustomNativeAd() {

//        mExtras = new HashMap<String, Object>();
    }


    /**
     * Check if it's template ad
     */
    @Override
    public boolean isNativeExpress() {
        return false;
    }

    /**
     * Returns the ad's MediaView
     *
     * @param object
     * @return
     */
    @Override
    public View getAdMediaView(Object... object) {
        return null;
    }

    /**
     * Returns the ad's IconView
     *
     * @return
     */
    @Override
    public View getAdIconView() {
        return null;
    }

    // Getters

    /**
     * Returns the String corresponding to the ad's title.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the String corresponding to the ad's body text.
     */

    public String getDescriptionText() {
        return mText;
    }

    /**
     * Returns the String url corresponding to the ad's main image.
     */

    public String getMainImageUrl() {
        return mMainImageUrl;
    }

    /**
     * Returns the String url corresponding to the ad's icon image.
     */

    public String getIconImageUrl() {
        return mIconImageUrl;
    }

    /**
     * Returns the Call To Action String (i.e. "Download" or "Learn More") associated with this ad.
     */

    public String getCallToActionText() {
        return mCallToAction;
    }

    /**
     * For app install ads, this returns the associated star rating (on a 5 star scale) for the
     * advertised app. Note that this method may return null if the star rating was either never set
     * or invalid.
     */

    final public Double getStarRating() {
        return mStarRating;
    }

    final public String getVideoUrl() {
        return mVideoUrl;
    }

    final public String getAdChoiceIconUrl() {
        return mAdChoiceIconUrl;
    }

    public String getAdFrom() {
        return mAdFrom;
    }

    final public List<String> getImageUrlList() {
        return mImageUrlList;
    }


//    final public Object getExtra(final String key) {
//        if (TextUtils.isEmpty(key)) {
//            return null;
//        }
//        return mExtras.get(key);
//    }

    /**
     * Returns a copy of the extras map, reflecting additional ad content not reflected in any
     * of the above hardcoded setters. This is particularly useful for passing down custom fields
     * with MoPub's direct-sold native ads or from mediated networks that pass back additional
     * fields.
     */

//    final public Map<String, Object> getExtras() {
//        return new HashMap<String, Object>(mExtras);
//    }

    /**
     * Returns the String url that the device will attempt to resolve when the ad is clicked.
     */

//    final public String getClickDestinationUrl() {
//        return mClickDestinationUrl;
//    }

    final public void setMainImageUrl(final String mainImageUrl) {
        mMainImageUrl = mainImageUrl;
    }

    final public void setIconImageUrl(final String iconImageUrl) {
        mIconImageUrl = iconImageUrl;
    }

//    final public void setClickDestinationUrl(final String clickDestinationUrl) {
//        mClickDestinationUrl = clickDestinationUrl;
//    }

    final public void setCallToActionText(final String callToAction) {
        mCallToAction = callToAction;
    }

    final public void setTitle(final String title) {
        mTitle = title;
    }

    final public void setDescriptionText(final String text) {
        mText = text;
    }

    final public void setStarRating(final Double starRating) {
        if (starRating == null) {
            mStarRating = null;
        } else if (starRating >= MIN_STAR_RATING && starRating <= MAX_STAR_RATING) {
            mStarRating = starRating;
        } else {
        }
    }

    final public void setVideoUrl(final String videoUrl) {
        mVideoUrl = videoUrl;
    }

    final public void setAdChoiceIconUrl(final String adChoiceIconUrl) {
        mAdChoiceIconUrl = adChoiceIconUrl;
    }

    final public void setAdFrom(final String adFrom) {
        mAdFrom = adFrom;
    }

    final public void setImageUrlList(final List<String> imageUrlList) {
        mImageUrlList = imageUrlList;
    }


    public Bitmap getAdLogo() {
        return null;
    }

//    final public void addExtra(final String key, final Object value) {
//        if (TextUtils.isEmpty(key)) {
//            return;
//        }
//        mExtras.put(key, value);
//    }

    // Lifecycle Handlers
    @Override
    public void prepare(final View view, final FrameLayout.LayoutParams layoutParams) {
    }

    @Override
    public void prepare(View view, List<View> clickViewList, FrameLayout.LayoutParams layoutParams) {

    }

    @Override
    public void clear(final View view) {
    }


    @Override
    public ViewGroup getCustomAdContainer() {
        return null;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void destroy() {

    }
}
