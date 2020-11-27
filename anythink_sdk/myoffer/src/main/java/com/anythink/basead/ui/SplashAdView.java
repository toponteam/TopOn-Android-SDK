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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.basead.buiness.OfferAdFunctionUtil;
import com.anythink.basead.buiness.OfferClickController;
import com.anythink.basead.listeners.AdListener;
import com.anythink.basead.myoffer.manager.MyOfferImpressionRecordManager;
import com.anythink.basead.ui.component.RoundImageView;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.entity.MyOfferSetting;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.BitmapUtil;
import com.anythink.core.common.utils.CommonUtil;

public class SplashAdView extends RelativeLayout {
    MyOfferAd mMyOfferAd;
    MyOfferSetting mMyOfferSettings;
    String mRequestId;

    TextView mSkipView;

    CountDownTimer mCountDownTimer;

    String mSkipString = "Skip";

    AdListener mMyOfferAdListener;

    OfferClickController mOfferClickController;

    public SplashAdView(Context context, String placementId, String requestId, MyOfferAd myOfferAd, MyOfferSetting myOfferSettings, AdListener myOfferAdListener) {
        super(context);

        mMyOfferAd = myOfferAd;
        mMyOfferSettings = myOfferSettings;
        mMyOfferAdListener = myOfferAdListener;
        mRequestId = requestId;

        mOfferClickController = new OfferClickController(context.getApplicationContext(), placementId, mMyOfferAd
                , mMyOfferSettings);

        if (myOfferSettings.getSplashOrientation() == 2) {
            LayoutInflater.from(context).inflate(CommonUtil.getResId(getContext(), "myoffer_splash_ad_land_layout", "layout"), this);
        } else {
            LayoutInflater.from(context).inflate(CommonUtil.getResId(getContext(), "myoffer_splash_ad_layout", "layout"), this);
        }


        initContentView();

        mSkipString = getResources().getString(CommonUtil.getResId(getContext(), "myoffer_splash_skip_text", "string"));
        mSkipView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_skip", "id"));

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOfferClickController != null) {
                    OfferAdFunctionUtil.sendAdTracking(mRequestId, mMyOfferAd, OfferAdFunctionUtil.CLICK_TYPE, "");
                    if (mMyOfferAdListener != null) {
                        mMyOfferAdListener.onAdClick();
                    }
                    mOfferClickController.startClick(mRequestId, new OfferClickController.ClickStatusCallback() {
                        @Override
                        public void clickStart() {
                        }

                        @Override
                        public void clickEnd() {

                        }

                        @Override
                        public void downloadApp(String url) {
                            OfferAdFunctionUtil.startDownloadApp(getContext(), mRequestId, mMyOfferSettings, mMyOfferAd, url);
                        }
                    });
                }
            }
        });
    }

    private void initContentView() {
        TextView titleView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_ad_title", "id"));
        TextView ctaView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_ad_install_btn", "id"));
        TextView descView = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_desc", "id"));
        TextView selfAdLogo = (TextView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_self_ad_logo", "id"));

        FrameLayout contentArea = (FrameLayout) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_ad_content_image_area", "id"));

        final RoundImageView bgView = (RoundImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_bg", "id"));


        final AppRatingView appRatingView = (AppRatingView) findViewById(CommonUtil.getResId(getContext(), "myoffer_rating_view", "id"));
        appRatingView.setStarNum(5);
        appRatingView.setRating(5);

        final RoundImageView logoView = (RoundImageView) findViewById(CommonUtil.getResId(getContext(), "myoffer_splash_ad_logo", "id"));
        if (!TextUtils.isEmpty(mMyOfferAd.getAdChoiceUrl())) {
            logoView.setVisibility(View.VISIBLE);
            int logoSize = logoView.getLayoutParams().width;
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, mMyOfferAd.getAdChoiceUrl()), logoSize, logoSize, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(url, mMyOfferAd.getAdChoiceUrl())) {
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


        contentArea.removeAllViews();
        int bigSize = contentArea.getLayoutParams().width;

        selfAdLogo.setVisibility(View.VISIBLE);
        final RoundImageView imageView = new RoundImageView(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setNeedRadiu(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        contentArea.addView(imageView, params);
        contentArea.setVisibility(View.VISIBLE);

        ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE
                , mMyOfferAd.getEndCardImageUrl()), getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().widthPixels * 627 / 1200, new ImageLoader.ImageLoaderListener() {
            @Override
            public void onSuccess(String url, Bitmap bitmap) {
                if (TextUtils.equals(url, mMyOfferAd.getEndCardImageUrl())) {
                    imageView.setImageBitmap(bitmap);
                    Bitmap blurBitmap = BitmapUtil.blurBitmap(getContext(), bitmap);
                    bgView.setImageBitmap(blurBitmap);
                }
            }

            @Override
            public void onFail(String url, String errorMsg) {

            }
        });


        if (mMyOfferSettings.getSplashOrientation() == 2) {
            if (!TextUtils.isEmpty(mMyOfferAd.getDesc())) {
                titleView.setText(mMyOfferAd.getDesc());
                titleView.setVisibility(View.VISIBLE);
            } else {
                titleView.setVisibility(View.INVISIBLE);
            }
        } else {
            if (!TextUtils.isEmpty(mMyOfferAd.getTitle())) {
                titleView.setText(mMyOfferAd.getTitle());
                titleView.setVisibility(View.VISIBLE);
            } else {
                titleView.setVisibility(View.INVISIBLE);
            }
        }


        if (!TextUtils.isEmpty(mMyOfferAd.getCtaText())) {
            ctaView.setText(mMyOfferAd.getCtaText());
            ctaView.setVisibility(View.VISIBLE);
        } else {
            ctaView.setVisibility(View.GONE);
        }

        if (descView != null) {
            if (!TextUtils.isEmpty(mMyOfferAd.getDesc())) {
                descView.setText(mMyOfferAd.getDesc());
                descView.setVisibility(View.VISIBLE);
            } else {
                descView.setVisibility(View.GONE);
            }
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == VISIBLE) {

        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            notifyShow();
        }
    }

    boolean isStartCountDown = false;
    boolean isFinishCountDown = false;

    private void startCountdown() {
        if (isStartCountDown) {
            return;
        }

        /**Only once to record**/
        MyOfferImpressionRecordManager.getInstance(getContext()).recordImpression(mMyOfferAd);
        OfferAdFunctionUtil.sendAdTracking(mRequestId, mMyOfferAd, OfferAdFunctionUtil.IMPRESSION_TYPE, "");

        isStartCountDown = true;

        mSkipView.setVisibility(VISIBLE);
        mSkipView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMyOfferSettings.getCanSplashSkip() == 0 || isFinishCountDown) {
                    if (mCountDownTimer != null) {
                        mCountDownTimer.cancel();
                    }
                    if (mMyOfferAdListener != null) {
                        mMyOfferAdListener.onAdClosed();
                    }
                }

            }
        });

        isFinishCountDown = false;

        mCountDownTimer = new CountDownTimer(mMyOfferSettings.getSplashCountdownTime(), 1000L) {
            @Override
            public void onTick(long l) {

                if (mMyOfferSettings.getCanSplashSkip() == 0) {
                    mSkipView.setText((l / 1000 + 1) + "s " + mSkipString);
                } else {
                    mSkipView.setText((l / 1000 + 1) + " s");
                }

            }

            @Override
            public void onFinish() {
                mSkipView.setText(mSkipString);
                isFinishCountDown = true;
                if (mMyOfferAdListener != null) {
                    mMyOfferAdListener.onAdClosed();
                }
            }
        };

        mCountDownTimer.start();
    }

    private void notifyShow() {

        if (mMyOfferAdListener != null) {
            mMyOfferAdListener.onAdShow();
        }

        startCountdown();
    }
}
