/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.unitgroup.api;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.anythink.nativead.api.NativeAdInteractionType;
import com.anythink.nativead.unitgroup.BaseNativeAd;

import java.util.List;
import java.util.Map;


public class CustomNativeAd extends BaseNativeAd {

    static final double MIN_STAR_RATING = 0;
    static final double MAX_STAR_RATING = 5;

    public static String IS_AUTO_PLAY_KEY = "is_auto_play";

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

    private Map<String, Object> mNetworkInfoMap;

    private View adLogoView;
    private ExtraInfo extraInfo;

    private View.OnClickListener mCloseViewListener;

    private int nInteractionType = NativeAdInteractionType.UNKNOW;

    public CustomNativeAd() {

//        mExtras = new HashMap<String, Object>();
    }

    public int getNativeAdInteractionType() {
        return nInteractionType;
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
    final public void setNativeInteractionType(int interactionType) {
        this.nInteractionType = interactionType;
    }

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

    final public View getAdLogoView() {
        return adLogoView;
    }

    final public void setAdLogoView(View adLogoView) {
        this.adLogoView = adLogoView;
    }

    public ExtraInfo getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(ExtraInfo extraInfo) {
        this.extraInfo = extraInfo;
    }

    final public void setNetworkInfoMap(Map<String, Object> networkInfoMap) {
        mNetworkInfoMap = networkInfoMap;
    }

    final public Map<String, Object> getNetworkInfoMap() {
        return mNetworkInfoMap;
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
    final public void bindDislikeListener(View.OnClickListener onClickListener) {
        this.mCloseViewListener = onClickListener;

        ExtraInfo extraInfo = getExtraInfo();
        if (extraInfo != null) {
            View closeView = extraInfo.getCloseView();
            if (closeView != null) {
                closeView.setOnClickListener(mCloseViewListener);
            }
        }
    }

    final public boolean checkHasCloseViewListener() {
        return mCloseViewListener != null;
    }

    public void impressionTrack(View adView) {

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
        mCloseViewListener = null;
        extraInfo = null;
    }

    /**
     * Only For GDT
     */
    public void registerDownloadConfirmListener() {

    }

    public void unregeisterDownloadConfirmListener() {

    }

    public static class ExtraInfo {
        int parentViewId;
        int titleViewId;
        int sourceViewId;
        int descriptionViewId;
        int mainImageViewId;
        int adLogoViewId;
        int calltoActionViewId;
        int iconViewId;
        View closeView;
        List<View> creativeViews;
        List<View> customViews; //Only for GDT open Ad directly

        public View getCloseView() {
            return closeView;
        }

        private void setCloseView(View closeView) {
            this.closeView = closeView;
        }

        public int getParentViewId() {
            return parentViewId;
        }

        private void setParentViewId(int parentViewId) {
            this.parentViewId = parentViewId;
        }

        public int getTitleViewId() {
            return titleViewId;
        }

        private void setTitleViewId(int titleViewId) {
            this.titleViewId = titleViewId;
        }

        public int getSourceViewId() {
            return sourceViewId;
        }

        private void setSourceViewId(int sourceViewId) {
            this.sourceViewId = sourceViewId;
        }

        public int getDescriptionViewId() {
            return descriptionViewId;
        }

        private void setDescriptionViewId(int descriptionViewId) {
            this.descriptionViewId = descriptionViewId;
        }

        public int getMainImageViewId() {
            return mainImageViewId;
        }

        private void setMainImageViewId(int mainImageViewId) {
            this.mainImageViewId = mainImageViewId;
        }

        public int getAdLogoViewId() {
            return adLogoViewId;
        }

        private void setAdLogoViewId(int adLogoViewId) {
            this.adLogoViewId = adLogoViewId;
        }

        public int getCalltoActionViewId() {
            return calltoActionViewId;
        }

        private void setCalltoActionViewId(int calltoActionViewId) {
            this.calltoActionViewId = calltoActionViewId;
        }

        public int getIconViewId() {
            return iconViewId;
        }

        private void setIconViewId(int iconViewId) {
            this.iconViewId = iconViewId;
        }

        public List<View> getCreativeViews() {
            return creativeViews;
        }

        private void setCreativeViews(List<View> creativeViews) {
            this.creativeViews = creativeViews;
        }

        public List<View> getCustomViews() {
            return customViews;
        }

        private void setCustomViews(List<View> customViews) {
            this.customViews = customViews;
        }

        public static class Builder {
            int parentViewId;
            int titleViewId;
            int sourceViewId;
            int descriptionViewId;
            int mainImageViewId;
            int adLogoViewId;
            int calltoActionViewId;
            int iconViewId;
            View closeView;
            List<View> creativeViews;
            List<View> customViews; //Only for GDT open Ad directly

            public Builder setParentId(int parentViewId) {
                this.parentViewId = parentViewId;
                return this;
            }

            public Builder setCloseView(View closeView) {
                this.closeView = closeView;
                return this;
            }

            public Builder setTitleViewId(int titleViewId) {
                this.titleViewId = titleViewId;
                return this;
            }

            public Builder setSourceViewId(int sourceViewId) {
                this.sourceViewId = sourceViewId;
                return this;
            }

            public Builder setDescriptionViewId(int descriptionViewId) {
                this.descriptionViewId = descriptionViewId;
                return this;
            }

            public Builder setMainImageViewId(int mainImageViewId) {
                this.mainImageViewId = mainImageViewId;
                return this;
            }

            public Builder setAdLogoViewId(int adLogoViewId) {
                this.adLogoViewId = adLogoViewId;
                return this;
            }

            public Builder setCalltoActionViewId(int calltoActionViewId) {
                this.calltoActionViewId = calltoActionViewId;
                return this;
            }

            public Builder setIconViewId(int iconViewId) {
                this.iconViewId = iconViewId;
                return this;
            }

            public Builder setCreativeViewList(List<View> creativeViews) {
                this.creativeViews = creativeViews;
                return this;
            }

            public Builder setCustomViewList(List<View> customViews) {
                this.customViews = customViews;
                return this;
            }


            public ExtraInfo build() {
                ExtraInfo extraInfo = new ExtraInfo();

                extraInfo.setParentViewId(this.parentViewId);
                extraInfo.setCloseView(this.closeView);
                extraInfo.setAdLogoViewId(this.adLogoViewId);
                extraInfo.setCalltoActionViewId(this.calltoActionViewId);
                extraInfo.setCreativeViews(this.creativeViews);
                extraInfo.setDescriptionViewId(this.descriptionViewId);
                extraInfo.setIconViewId(this.iconViewId);
                extraInfo.setTitleViewId(this.titleViewId);
                extraInfo.setMainImageViewId(this.mainImageViewId);
                extraInfo.setSourceViewId(this.sourceViewId);
                extraInfo.setCustomViews(this.customViews);


                return extraInfo;
            }
        }
    }

}
