/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.entity.UserOperateRecord;
import com.anythink.basead.listeners.AdEventListener;
import com.anythink.basead.ui.component.RoundImageView;
import com.anythink.core.common.base.Const;
import com.anythink.core.common.base.SDKContext;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdRequestInfo;
import com.anythink.core.common.entity.OwnBaseAdContent;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.BitmapUtil;
import com.anythink.core.common.utils.CommonUtil;

public class SplashAdView extends BaseAdView {

    TextView mSkipView;
    String mSkipString = "Skip";
    CountDownTimer mCountDownTimer;

    AdEventListener mAdEventListener;

    LoadingView mLoadingView;

    int mBitmapWidth;
    int mBitmapHeight;

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBaseAdRequestInfo.baseAdSetting != null && mBaseAdRequestInfo.baseAdSetting.getEndCardClickArea() == 0) {//fullscreen
                SplashAdView.super.onClick();
            }
        }
    };

    private final OnClickListener mCreativeClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SplashAdView.super.onClick();
        }
    };

    public SplashAdView(Context context) {
        super(context);
    }

    public SplashAdView(Context context, BaseAdRequestInfo baseAdRequestInfo, BaseAdContent baseAdContent, AdEventListener adEventListener) {
        super(context, baseAdRequestInfo, baseAdContent);

        mAdEventListener = adEventListener;

        resigterImpressionView();
        registerListener();
    }


    @Override
    protected void initContentView() {

        if (mBaseAdRequestInfo.baseAdSetting.getSplashOrientation() == 2) {
            LayoutInflater.from(getContext()).inflate(CommonUtil.getResId(getContext(), "myoffer_splash_ad_land_layout", "layout"), this);
        } else {
            LayoutInflater.from(getContext()).inflate(CommonUtil.getResId(getContext(), "myoffer_splash_ad_layout", "layout"), this);
        }

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
        TextView titleView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_ad_title", "id"));
        TextView ctaView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_ad_install_btn", "id"));
        TextView descView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_desc", "id"));
        TextView selfAdLogo = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_self_ad_logo", "id"));

        FrameLayout contentArea = (FrameLayout) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_ad_content_image_area", "id"));
        final RoundImageView bgView = (RoundImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_bg", "id"));
        final RoundImageView iconView = (RoundImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_icon", "id"));

        mSkipString = getResources().getString(CommonUtil.getResId(getContext(), "myoffer_splash_skip_text", "string"));
        mSkipView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_skip", "id"));

        //ad choice
        final RoundImageView logoView = (RoundImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_ad_logo", "id"));
        if (!TextUtils.isEmpty(mBaseAdContent.getAdChoiceUrl())) {
            logoView.setVisibility(View.VISIBLE);
            int logoSize = logoView.getLayoutParams().width;
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mBaseAdContent.getAdChoiceUrl()), logoSize, logoSize, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(url, mBaseAdContent.getAdChoiceUrl())) {
                        logoView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });

        } else {
            logoView.setVisibility(View.GONE);
        }

        mClickViewLists.add(logoView);

        //single picture
        if (isSinglePicture()) {
            titleView.setVisibility(View.GONE);
            ctaView.setVisibility(View.GONE);
            descView.setVisibility(View.GONE);
            contentArea.setVisibility(View.GONE);
            iconView.setVisibility(View.GONE);

            //main image (do not trigger click event for adx & onlineapi when only show picture
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE
                    , mBaseAdContent.getEndCardImageUrl()), getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels * 627 / 1200, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(url, mBaseAdContent.getEndCardImageUrl())) {
                        mBitmapWidth = bitmap.getWidth();
                        mBitmapHeight = bitmap.getHeight();

                        RelativeLayout root = (RelativeLayout) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_root", "id"));

                        ImageView fullscreenImageView = new ImageView(getContext());
                        fullscreenImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        fullscreenImageView.setImageBitmap(bitmap);

                        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        root.addView(fullscreenImageView, 1, layoutParams);

                        Bitmap blurBitmap = BitmapUtil.blurBitmap(getContext(), bitmap);
                        bgView.setImageBitmap(blurBitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });

            //AD
            if (SDKContext.getInstance().isAdLogoVisible()) {
                selfAdLogo.setVisibility(View.VISIBLE);

            } else {
                selfAdLogo.setVisibility(View.GONE);
            }
            mClickViewLists.add(selfAdLogo);


            //cta
            if (mBaseAdRequestInfo.baseAdSetting != null && mBaseAdRequestInfo.baseAdSetting.getEndCardClickArea() != 0) {

                ctaView.setVisibility(VISIBLE);

                if (!TextUtils.isEmpty(mBaseAdContent.getCtaText())) {
                    ctaView.setText(mBaseAdContent.getCtaText());
                } else {
                    ctaView.setText(CommonUtil.getResId(getContext(), "myoffer_cta_learn_more", "string"));
                }

                LayoutParams layoutParams = (LayoutParams) ctaView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    ctaView.setLayoutParams(layoutParams);
                }

                mClickViewLists.add(ctaView);
            }

            return;
        }

        //icon
        if (mBaseAdContent instanceof OwnBaseAdContent && !TextUtils.isEmpty(mBaseAdContent.getIconUrl())) {
            iconView.setVisibility(View.VISIBLE);
            iconView.setNeedRadiu(true);
            iconView.setRadiusInDip(3);
            int logoSize = iconView.getLayoutParams().width;
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mBaseAdContent.getIconUrl()), logoSize, logoSize, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(url, mBaseAdContent.getIconUrl())) {
                        iconView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });

        } else {
            iconView.setVisibility(View.GONE);

            //update layout element
            if (mBaseAdRequestInfo.baseAdSetting.getSplashOrientation() == 1) {//portrait
                LayoutParams titleViewLayoutParams = (LayoutParams) titleView.getLayoutParams();
                if (titleViewLayoutParams != null) {
                    titleViewLayoutParams.topMargin = CommonUtil.dip2px(getContext(), 60);
                }
            }

        }
        mClickViewLists.add(iconView);

        //app rating
        final AppRatingView appRatingView = (AppRatingView) findViewById(CommonUtil.getResId(getContext(), "myoffer_rating_view", "id"));
        int rating = mBaseAdContent.getRating();
        if (rating > 5) {
            appRatingView.setVisibility(View.VISIBLE);
            appRatingView.setStarNum(5);
            appRatingView.setRating(5);
        } else if (rating > 0) {
            appRatingView.setVisibility(View.VISIBLE);
            appRatingView.setStarNum(5);
            appRatingView.setRating(rating);
        } else {
            appRatingView.setVisibility(View.GONE);
        }
        mClickViewLists.add(appRatingView);

        //AD
        if (SDKContext.getInstance().isAdLogoVisible()) {
            selfAdLogo.setVisibility(View.VISIBLE);
        } else {
            selfAdLogo.setVisibility(View.GONE);
        }
        mClickViewLists.add(selfAdLogo);


        //main image
        contentArea.removeAllViews();
//        int bigSize = contentArea.getLayoutParams().width;
        final RoundImageView imageView = new RoundImageView(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setNeedRadiu(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        contentArea.addView(imageView, params);
        contentArea.setVisibility(View.VISIBLE);

        ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE
                , mBaseAdContent.getEndCardImageUrl()), getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels * 627 / 1200, new ImageLoader.ImageLoaderListener() {
            @Override
            public void onSuccess(String url, Bitmap bitmap) {
                if (TextUtils.equals(url, mBaseAdContent.getEndCardImageUrl())) {
                    imageView.setImageBitmap(bitmap);
                    Bitmap blurBitmap = BitmapUtil.blurBitmap(getContext(), bitmap);
                    bgView.setImageBitmap(blurBitmap);
                }
            }

            @Override
            public void onFail(String url, String errorMsg) {

            }
        });
        mClickViewLists.add(imageView);

        //title
        if (mBaseAdRequestInfo.baseAdSetting.getSplashOrientation() == 2) {
            if (!TextUtils.isEmpty(mBaseAdContent.getTitle())) {
                titleView.setText(mBaseAdContent.getTitle());
                titleView.setVisibility(View.VISIBLE);
            } else {
                titleView.setVisibility(View.INVISIBLE);
            }
        } else {
            if (!TextUtils.isEmpty(mBaseAdContent.getTitle())) {
                titleView.setText(mBaseAdContent.getTitle());
                titleView.setVisibility(View.VISIBLE);
            } else {
                titleView.setVisibility(View.INVISIBLE);
            }
        }
        mClickViewLists.add(titleView);

        //cta
        if (!TextUtils.isEmpty(mBaseAdContent.getCtaText())) {
            ctaView.setText(mBaseAdContent.getCtaText());
            ctaView.setVisibility(View.VISIBLE);
        } else {
            ctaView.setVisibility(View.INVISIBLE);
        }
        mClickViewLists.add(ctaView);

        //desc
        if (!TextUtils.isEmpty(mBaseAdContent.getDesc())) {
            descView.setText(mBaseAdContent.getDesc());
            descView.setVisibility(View.VISIBLE);
        } else {
            descView.setVisibility(View.GONE);
        }
        mClickViewLists.add(descView);

    }

    private void resigterImpressionView() {
        SplashAdView.super.registerImpressionTracker(100, new Runnable() {
            @Override
            public void run() {
                if (isSinglePicture()) {
                    int impressionWidth = getWidth();
                    int impressionHeight = getHeight();

                    boolean isLandscape = mBitmapWidth > mBitmapHeight;

                    int portraitWidth = Math.min(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                    int portraitHeight = Math.max(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);

                    int validWidth;
                    int validHeight;

                    if (isLandscape) {
                        validWidth = (int) (portraitHeight * 0.75);
                        validHeight = (int) (portraitWidth * 0.75);
                    } else {
                        validWidth = (int) (portraitWidth * 0.75);
                        validHeight = (int) (portraitHeight * 0.75);
                    }

                    if (impressionWidth < validWidth) {
                        Log.e(Const.RESOURCE_HEAD, "Splash display width is less than 75% of screen width!");
                    } else if (impressionHeight < validHeight) {
                        Log.e(Const.RESOURCE_HEAD, "Splash display height is less than 75% of screen height!");
                    } else {
                        SplashAdView.super.onShow();
                    }
                } else {
                    SplashAdView.super.onShow();
                }
            }
        });

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
    }

    /**
     * for gdt onlineapi, portrait
     */
    private boolean isSinglePicture() {
        return mBaseAdContent instanceof OwnBaseAdContent &&
                OwnBaseAdContent.CREATIVE_TYPE_SINGLE_PICTURE == ((OwnBaseAdContent) mBaseAdContent).getCreativeType();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            startCountdown();
        }
    }

    boolean isStartCountDown = false;
    boolean isFinishCountDown = false;

    private void startCountdown() {
        if (isStartCountDown) {
            return;
        }
        isStartCountDown = true;

        SplashAdView.super.removeCache();

        mSkipView.setVisibility(VISIBLE);
        mSkipView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBaseAdRequestInfo.baseAdSetting.getCanSplashSkip() == 0 || isFinishCountDown) {
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                    if (mAdEventListener != null) {
                        mAdEventListener.onAdClosed();
                    }
                }

            }
        });

        isFinishCountDown = false;

        mCountDownTimer = new CountDownTimer(mBaseAdRequestInfo.baseAdSetting.getSplashCountdownTime(), 1000L) {
            @Override
            public void onTick(long l) {

                if (mBaseAdRequestInfo.baseAdSetting.getCanSplashSkip() == 0) {
                    mSkipView.setText((l / 1000 + 1) + "s " + mSkipString);
                } else {
                    mSkipView.setText((l / 1000 + 1) + " s");
                }

            }

            @Override
            public void onFinish() {
                mSkipView.setText(mSkipString);
                isFinishCountDown = true;
                if (mAdEventListener != null) {
                    mAdEventListener.onAdClosed();
                }
            }
        };

        mCountDownTimer.start();
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

    @Override
    protected void onClickStart() {
        if (mBaseAdContent instanceof OwnBaseAdContent) {
            if (mLoadingView == null) {
                mLoadingView = new LoadingView(SplashAdView.this);
            }
            post(new Runnable() {
                @Override
                public void run() {
                    mLoadingView.startLoading();
                }
            });
        }
    }

    @Override
    protected void onClickEnd() {
        if (mBaseAdContent instanceof OwnBaseAdContent) {
            if (mLoadingView != null) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingView.hide();
                    }
                });
            }
        }
    }

    @Override
    protected void destroy() {
        super.destroy();

        this.mAdEventListener = null;
    }
}
