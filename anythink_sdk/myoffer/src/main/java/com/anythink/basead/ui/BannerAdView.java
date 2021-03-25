/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.resource.OfferResourceState;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.ui.component.RoundImageView;
import com.anythink.basead.ui.util.ViewUtil;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.res.image.RecycleImageView;
import com.anythink.core.common.utils.BitmapUtil;
import com.anythink.core.common.utils.CommonLogUtil;
import com.anythink.core.common.utils.CommonUtil;

public class BannerAdView extends BaseAdView {

    public static final String TAG = BannerAdView.class.getSimpleName();

    AdEventListener mAdEventListener;

    boolean mNeedShowMainImage;
    String mBannerSize;
    int mWidth;
    int mHeight;

    private View mCloseView;

    private int mMode = MODE_ASSEMBLE;
    private static final int MODE_PURE_PICTURE = 1;
    private static final int MODE_ASSEMBLE = 2;

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (BannerAdView.MODE_ASSEMBLE == mMode) {
                if (mBaseAdRequestInfo.baseAdSetting != null && mBaseAdRequestInfo.baseAdSetting.getEndCardClickArea() == 0) {//fullscreen
                    BannerAdView.super.onClick();
                }
            } else {
                BannerAdView.super.onClick();
            }
        }
    };

    private final OnClickListener mCreativeClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            BannerAdView.super.onClick();
        }
    };

    public BannerAdView(Context context) {
        super(context);
    }

    public BannerAdView(Context context, BaseAdRequestInfo baseAdRequestInfo, BaseAdContent baseAdContent, AdEventListener adEventListener) {
        super(context, baseAdRequestInfo, baseAdContent);

        mAdEventListener = adEventListener;

        resigterImpressionView();
        registerListener();
    }

    @Override
    protected void initContentView() {

        //todo 判断是否MRAID 调用initMraid()
//        if () {
//            initMraid();
//        } else {
        initView();
//        }
    }

    private void initMraid() {

    }

    private void initView() {
        String bannerSize = mBaseAdRequestInfo.baseAdSetting.getBannerSize();

        String bannerUrl = mBaseAdContent instanceof OwnBaseAdContent ? mBaseAdContent.getEndCardImageUrl() : null;
        String layoutName;
        switch (bannerSize) {
            case MyOfferSetting.BANNER_SIZE_320x90:
                mBannerSize = MyOfferSetting.BANNER_SIZE_320x90;
                mWidth = 320;
                mHeight = 90;
                layoutName = "myoffer_banner_ad_layout_320x90";
                if (bannerUrl == null && mBaseAdContent instanceof MyOfferAd) {
                    bannerUrl = ((MyOfferAd) mBaseAdContent).getBanner320x90Url();
                }
                mNeedShowMainImage = true;
                break;

            case MyOfferSetting.BANNER_SIZE_300x250:
                mBannerSize = MyOfferSetting.BANNER_SIZE_300x250;
                mWidth = 300;
                mHeight = 250;
                layoutName = "myoffer_banner_ad_layout_300x250";
                if (bannerUrl == null && mBaseAdContent instanceof MyOfferAd) {
                    bannerUrl = ((MyOfferAd) mBaseAdContent).getBanner300x250Url();
                }
                mNeedShowMainImage = true;
                break;

            case MyOfferSetting.BANNER_SIZE_728x90:
                mBannerSize = MyOfferSetting.BANNER_SIZE_728x90;
                mWidth = 728;
                mHeight = 90;
                layoutName = "myoffer_banner_ad_layout_728x90";
                if (bannerUrl == null && mBaseAdContent instanceof MyOfferAd) {
                    bannerUrl = ((MyOfferAd) mBaseAdContent).getBanner728x90Url();
                }
                mNeedShowMainImage = true;
                break;

            case MyOfferSetting.BANNER_SIZE_320x50:
            default:
                mBannerSize = MyOfferSetting.BANNER_SIZE_320x50;
                mWidth = 320;
                mHeight = 50;
                layoutName = "myoffer_banner_ad_layout_320x50";
                if (bannerUrl == null && mBaseAdContent instanceof MyOfferAd) {
                    bannerUrl = ((MyOfferAd) mBaseAdContent).getBanner320x50Url();
                }
                break;
        }

        if (BannerAdView.MODE_PURE_PICTURE == this.getBannerMode(bannerUrl)) {//Only picture
            CommonLogUtil.d(TAG, "mode: pure picture");
            LayoutInflater.from(getContext()).inflate(CommonUtil.getResId(getContext(), "myoffer_banner_ad_layout_pure_picture", "layout"), this);
            initPurePictureBannerView(bannerUrl);
        } else {//assemble banner view
            CommonLogUtil.d(TAG, "mode: assemble banner");
            LayoutInflater.from(getContext()).inflate(CommonUtil.getResId(getContext(), layoutName, "layout"), this);
            assembleBannerView();
        }
    }

    private void resigterImpressionView() {
        registerImpressionTracker(new Runnable() {
            @Override
            public void run() {
                BannerAdView.super.onShow();
            }
        });
    }

    private int getBannerMode(String bannerUrl) {
        int result = BannerAdView.MODE_PURE_PICTURE;
        if (mBaseAdContent instanceof OwnBaseAdContent) {
            int creativeType = ((OwnBaseAdContent) mBaseAdContent).getCreativeType();
            switch (creativeType) {
                case OwnBaseAdContent.CREATIVE_TYPE_SINGLE_PICTURE:
                    result = BannerAdView.MODE_PURE_PICTURE;
                    break;
                case OwnBaseAdContent.CREATIVE_TYPE_SINGLE_PICTURE_AND_TEXT:
                    result = BannerAdView.MODE_ASSEMBLE;
                    break;
            }
        } else if (mBaseAdContent instanceof MyOfferAd) {
            if (!TextUtils.isEmpty(bannerUrl) && OfferResourceState.isExist(bannerUrl)) {//Only picture
                result = BannerAdView.MODE_PURE_PICTURE;
            } else {
                result = BannerAdView.MODE_ASSEMBLE;
            }
        }
        this.mMode = result;
        return result;
    }


    private void initPurePictureBannerView(final String bannerUrl) {

        RelativeLayout rootView = (RelativeLayout) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_root", "id"));
        mCloseView = (ImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_close", "id"));
        View adTextView = findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_ad_text", "id"));

        if (0 == mBaseAdRequestInfo.baseAdSetting.getIsShowCloseButton()) {//show close button
            mCloseView.setVisibility(VISIBLE);

            if (TextUtils.equals(MyOfferSetting.BANNER_SIZE_728x90, mBannerSize)) {
                ViewGroup.LayoutParams layoutParams = mCloseView.getLayoutParams();
                layoutParams.width = CommonUtil.dip2px(getContext(), 23);
                layoutParams.height = CommonUtil.dip2px(getContext(), 23);
                mCloseView.setLayoutParams(layoutParams);
            }
        } else {//hide close button
            mCloseView.setVisibility(GONE);
        }

//        int width = CommonUtil.dip2px(getContext(), mWidth);
//        int height = CommonUtil.dip2px(getContext(), mHeight);

        //limit parent's size
        LayoutParams lp = (LayoutParams) rootView.getLayoutParams();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.MATCH_PARENT;
        rootView.setLayoutParams(lp);

        //blur background
        final RecycleImageView bgIv = new RecycleImageView(getContext());
        bgIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(bgIv, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        //main image
        final RecycleImageView imageView = new RecycleImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, bannerUrl), new ImageLoader.ImageLoaderListener() {
            @Override
            public void onSuccess(String url, final Bitmap bitmap) {
                if (TextUtils.equals(bannerUrl, url)) {
                    imageView.setImageBitmap(bitmap);

                    //update image view for click area
                    post(new Runnable() {
                        @Override
                        public void run() {
                            int viewWidth = getWidth();
                            int viewHeight = getHeight();
                            float ratio = (float) bitmap.getWidth() / bitmap.getHeight();

                            int[] fitSize = ViewUtil.getFitSize(viewWidth, viewHeight, ratio);

                            ViewGroup.LayoutParams imageLayoutParams = imageView.getLayoutParams();
                            if (imageLayoutParams != null) {
                                imageLayoutParams.width = fitSize[0];
                                imageLayoutParams.height = fitSize[1];

                                imageView.setLayoutParams(imageLayoutParams);
                            }
                        }
                    });

                    Bitmap blurBitmap = BitmapUtil.blurBitmap(getContext(), bitmap);
                    bgIv.setImageBitmap(blurBitmap);
                }
            }

            @Override
            public void onFail(String url, String errorMsg) {

            }
        });
        mClickViewLists.add(imageView);
        LayoutParams imageLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        imageLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(imageView, 1, imageLayoutParams);

        //ad choice
        if (!TextUtils.isEmpty(mBaseAdContent.getAdChoiceUrl())) {
            final ImageView adChoiceView = (ImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_self_ad_logo", "id"));
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mBaseAdContent.getAdChoiceUrl()), new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(mBaseAdContent.getAdChoiceUrl(), url)) {
                        adChoiceView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });

            mClickViewLists.add(adChoiceView);
        }

        //AD
        if (SDKContext.getInstance().isAdLogoVisible()) {
            adTextView.setVisibility(View.VISIBLE);
        } else {
            adTextView.setVisibility(View.GONE);
        }
        mClickViewLists.add(adTextView);

    }

    private void assembleBannerView() {
        final RoundImageView iconView = (RoundImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_icon", "id"));
        TextView titleView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_ad_title", "id"));
        TextView destView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_desc", "id"));
        TextView ctaView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_ad_install_btn", "id"));
        mCloseView = (ImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_close", "id"));
        View adTextView = findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_ad_text", "id"));

        ViewGroup.LayoutParams layoutParams;
        boolean isShowCloseButton = 0 == mBaseAdRequestInfo.baseAdSetting.getIsShowCloseButton();
        if (isShowCloseButton) {//show close button
            mCloseView.setVisibility(VISIBLE);
        } else {//hide close button
            mCloseView.setVisibility(GONE);

            LayoutParams lp;
            switch (mBannerSize) {
                case MyOfferSetting.BANNER_SIZE_320x50:
                    lp = ((LayoutParams) ctaView.getLayoutParams());
                    lp.rightMargin = CommonUtil.dip2px(getContext(), 10);
                    ctaView.setLayoutParams(lp);
                    break;
                case MyOfferSetting.BANNER_SIZE_320x90:
                    lp = ((LayoutParams) titleView.getLayoutParams());
                    lp.rightMargin = CommonUtil.dip2px(getContext(), 10);
                    titleView.setLayoutParams(lp);
                    break;
                case MyOfferSetting.BANNER_SIZE_728x90:
                    lp = ((LayoutParams) ctaView.getLayoutParams());
                    lp.rightMargin = CommonUtil.dip2px(getContext(), 46);
                    ctaView.setLayoutParams(lp);
                    break;
            }
        }

        //icon
        if (!TextUtils.isEmpty(mBaseAdContent.getIconUrl())) {
            layoutParams = iconView.getLayoutParams();
            iconView.setRadiusInDip(3);
            iconView.setNeedRadiu(true);
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mBaseAdContent.getIconUrl()), layoutParams.width, layoutParams.height, new ImageLoader.ImageLoaderListener() {

                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(mBaseAdContent.getIconUrl(), url)) {
                        iconView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });
        }
        mClickViewLists.add(iconView);

        //title、desc、cta
        titleView.setText(mBaseAdContent.getTitle());
        destView.setText(mBaseAdContent.getDesc());
        ctaView.setText(mBaseAdContent.getCtaText());
        mClickViewLists.add(titleView);
        mClickViewLists.add(destView);
        mClickViewLists.add(ctaView);


        //ad choice
        ImageView adChoiceView = null;
        if (!TextUtils.isEmpty(mBaseAdContent.getAdChoiceUrl())) {
            adChoiceView = (ImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_self_ad_logo", "id"));
            final ImageView finalAdChoiceView = adChoiceView;
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mBaseAdContent.getAdChoiceUrl()), new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(mBaseAdContent.getAdChoiceUrl(), url)) {
                        finalAdChoiceView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });
        }
        mClickViewLists.add(adChoiceView);

        //main image
        RoundImageView mainImageView = null;
        if (mNeedShowMainImage) {
            mainImageView = (RoundImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_banner_main_image", "id"));
            final RoundImageView finalMainImageView = mainImageView;
            if (!TextUtils.isEmpty(mBaseAdContent.getEndCardImageUrl())) {
                layoutParams = finalMainImageView.getLayoutParams();
                finalMainImageView.setRadiusInDip(3);
                finalMainImageView.setNeedRadiu(true);
                ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mBaseAdContent.getEndCardImageUrl()), new ImageLoader.ImageLoaderListener() {
                    @Override
                    public void onSuccess(String url, Bitmap bitmap) {
                        if (TextUtils.equals(mBaseAdContent.getEndCardImageUrl(), url)) {
                            finalMainImageView.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    public void onFail(String url, String errorMsg) {

                    }
                });

                mClickViewLists.add(mainImageView);
            }
        }
        mClickViewLists.add(mainImageView);

        //cta
        if (!TextUtils.isEmpty(mBaseAdContent.getCtaText())) {
            ctaView.setVisibility(View.VISIBLE);
        } else {
            ctaView.setVisibility(View.GONE);

            //update layout
            LayoutParams lp;
            switch (mBannerSize) {
                case MyOfferSetting.BANNER_SIZE_320x90:
                    lp = (LayoutParams) iconView.getLayoutParams();
                    lp.addRule(RelativeLayout.CENTER_VERTICAL);
                    lp.addRule(RelativeLayout.ALIGN_TOP, -1);
                    iconView.setLayoutParams(lp);
                    break;
                case MyOfferSetting.BANNER_SIZE_300x250:
                    lp = (LayoutParams) iconView.getLayoutParams();
                    lp.topMargin = CommonUtil.dip2px(getContext(), 25);
                    iconView.setLayoutParams(lp);
                    break;
                case MyOfferSetting.BANNER_SIZE_728x90:
                    lp = (LayoutParams) titleView.getLayoutParams();
                    lp.rightMargin = CommonUtil.dip2px(getContext(), 20);
                    titleView.setLayoutParams(lp);
                    break;
                case MyOfferSetting.BANNER_SIZE_320x50:
                default:
                    lp = (LayoutParams) titleView.getLayoutParams();
                    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    if (isShowCloseButton) {
                        lp.rightMargin = CommonUtil.dip2px(getContext(), 24);
                    } else {
                        lp.rightMargin = CommonUtil.dip2px(getContext(), 10);
                    }
                    titleView.setLayoutParams(lp);
                    break;
            }
        }

        //AD
        if (SDKContext.getInstance().isAdLogoVisible()) {
            adTextView.setVisibility(View.VISIBLE);
        } else {
            adTextView.setVisibility(View.GONE);
        }
        mClickViewLists.add(adTextView);

    }

    private void registerListener() {

        int size = mClickViewLists.size();
        View view;
        for (int i = 0; i < size; i++) {
            view = mClickViewLists.get(i);
            if (view != null) {
                view.setOnClickListener(mCreativeClickListener);
            }
        }

        setOnClickListener(mOnClickListener);

        mCloseView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdEventListener != null) {
                    mAdEventListener.onAdClosed();
                }
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        BannerAdView.super.removeCache();
    }

    @Override
    protected void notifyShow() {
        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.IMPRESSION_TYPE, mBaseAdContent, createUserOperateRecord());

        if (mAdEventListener != null) {
            mAdEventListener.onAdShow();
        }
    }

    @Override
    protected void notifyClick() {
        UserOperateRecord userOperateRecord = createUserOperateRecord();
        userOperateRecord.adClickRecord = getAdClickRecord();
        OfferAdFunctionUtil.sendAdTracking(OfferAdFunctionUtil.CLICK_TYPE, mBaseAdContent, userOperateRecord);

        if (mAdEventListener != null) {
            mAdEventListener.onAdClick();
        }
    }

    @Override
    protected void notifyDeeplinkCallback(boolean isSuccess) {
        if (mAdEventListener != null) {
            mAdEventListener.onDeeplinkCallback(isSuccess);
        }
    }
}
