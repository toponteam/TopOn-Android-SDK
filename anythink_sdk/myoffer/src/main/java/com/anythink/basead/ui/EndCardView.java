/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.basead.ui.component.RoundImageView;
import com.anythink.basead.ui.util.ViewUtil;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.BitmapUtil;
import com.anythink.core.common.utils.CommonUtil;

import java.util.Random;

public class EndCardView extends RelativeLayout {

    private OnEndCardListener mListener;
    private int mWidth;
    private int mHeight;

    private int mBlurBgIndex = 0;
    private int mEndCardIndex = 1;
    private int mCloseButtonIndex = 2;
    private int mLogoIndex = 3;
    private ImageView mEndCardIv;
    private RoundImageView bgIv;
    private ImageView mIvLogo;

    private int delayShowCloseButtonTime;
    private TextView mLearnMoreButton;
    private TextView mFeedbackButton;

    private BaseAdSetting mBaseAdSetting;

    private final OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mBaseAdSetting != null) {
                if (mBaseAdSetting.getEndCardClickArea() == 0) {//fullscreen
                    if (mListener != null) {
                        mListener.onClick();
                    }
                } else {
                    if (mLearnMoreButton != null && mLearnMoreButton.isShown()) {
                        if (v == mLearnMoreButton) {//learn more button
                            if (mListener != null) {
                                mListener.onClick();
                            }
                        }
                    }
                }
            }
        }

    };

    public EndCardView(ViewGroup container, int width, int height, BaseAdContent baseAdContent, BaseAdSetting baseAdSetting,
                       boolean needShowAdChoice, boolean needHideFeedbackButton, boolean needShowLearnMoreButton, OnEndCardListener listener) {
        super(container.getContext());
        this.mListener = listener;

        this.mWidth = width;
        this.mHeight = height;

        this.delayShowCloseButtonTime = getDelayShowCloseButtonTime(baseAdSetting);
        this.mBaseAdSetting = baseAdSetting;

        init(baseAdContent, needShowAdChoice, needHideFeedbackButton, needShowLearnMoreButton);
        attachTo(container);

        loadBitmap(baseAdContent);
    }

    /**
     * get delay time when show close button in endcard page
     */
    private int getDelayShowCloseButtonTime(BaseAdSetting baseAdSetting) {
        if (baseAdSetting != null) {
            int probability = (int) (baseAdSetting.getProbabilityForDelayShowCloseButtonInEndCard() / 100f);
            if (probability == 0) {
                return 0;
            }

            Random randomObject = new Random();
            int random = randomObject.nextInt(100);
            if (random > probability) {
                return 0;
            }

            int minDelayTime = baseAdSetting.getMinDelayTimeWhenShowCloseButton();
            int maxDelayTime = baseAdSetting.getMaxDelayTimeWhenShowCloseButton();

            if (maxDelayTime <= 0) {
                return 0;
            }

            if (minDelayTime == maxDelayTime) {
                return minDelayTime;
            }

            try {
                return randomObject.nextInt(maxDelayTime - minDelayTime) + minDelayTime;
            } catch (Throwable e) {
                e.printStackTrace();
                return 0;
            }
        }

        return 0;
    }

    private void loadBitmap(final BaseAdContent myOfferAd) {
        try {
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, myOfferAd.getEndCardImageUrl()), mWidth, mHeight, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, final Bitmap bitmap) {
                    if (TextUtils.equals(url, myOfferAd.getEndCardImageUrl())) {
                        mEndCardIv.setImageBitmap(bitmap);

                        post(new Runnable() {
                            @Override
                            public void run() {
                                int viewWidth = getWidth();
                                int viewHeight = getHeight();
                                float ratio = (float) bitmap.getWidth() / bitmap.getHeight();

                                int[] fitSize = ViewUtil.getFitSize(viewWidth, viewHeight, ratio);

                                ViewGroup.LayoutParams layoutParams = mEndCardIv.getLayoutParams();
                                if (layoutParams != null) {
                                    layoutParams.width = fitSize[0];
                                    layoutParams.height = fitSize[1];

                                    mEndCardIv.setLayoutParams(layoutParams);
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
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(BaseAdContent baseAdContent, boolean needShowAdChoice, final boolean needShowFeedbackButton, boolean needShowLearnMoreButton) {
        bgIv = new RoundImageView(getContext());
        bgIv.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mEndCardIv = new RoundImageView(getContext());

        LayoutParams rl_bg = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        LayoutParams rl_endcard = new LayoutParams(mWidth, mHeight);
        rl_endcard.addRule(RelativeLayout.CENTER_IN_PARENT);

        addView(bgIv, mBlurBgIndex, rl_bg);
        addView(mEndCardIv, mEndCardIndex, rl_endcard);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!needShowFeedbackButton) {
                    initFeedbackButton();
                }
                initCloseButton();
            }
        }, this.delayShowCloseButtonTime);

        if (needShowAdChoice) {
            initAdChoice(baseAdContent);
        }

        if (needShowLearnMoreButton) {
            initLearnMoreButton();
        }

//        mEndCardIv.setOnClickListener(mClickListener);

        setOnClickListener(mClickListener);

    }

    private void initAdChoice(BaseAdContent baseAdContent) {
        mIvLogo = new RoundImageView(getContext());

        LayoutParams rl_logo = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rl_logo.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rl_logo.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(mIvLogo, rl_logo);

        final String logoUrl = baseAdContent.getAdChoiceUrl();//logo
        if (!TextUtils.isEmpty(logoUrl)) {
            ViewGroup.LayoutParams lp = mIvLogo.getLayoutParams();
            int width = lp.width;
            int height = lp.height;
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, logoUrl), width, height, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(url, logoUrl)) {
                        mIvLogo.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });
        }

//        mIvLogo.setOnClickListener(mClickListener);
    }

    private void initCloseButton() {

        ImageView mCloseBtn = new ImageView(getContext());
        mCloseBtn.setImageResource(CommonUtil.getResId(getContext(), "myoffer_video_close", "drawable"));

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 29, getContext().getResources().getDisplayMetrics());
        int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getContext().getResources().getDisplayMetrics());
        int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 19, getContext().getResources().getDisplayMetrics());
        LayoutParams rl = new LayoutParams(size, size);
        rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rl.rightMargin = rightMargin;
        rl.topMargin = topMargin;
        addView(mCloseBtn, mCloseButtonIndex, rl);

        //扩大点击区域
        ViewUtil.expandTouchArea(mCloseBtn, size / 2);

        mCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCloseEndCard();
                }
            }
        });
    }

    private void initFeedbackButton() {
        mFeedbackButton = new TextView(getContext());
        mFeedbackButton.setText(CommonUtil.getResId(getContext(), "myoffer_feedback_text", "string"));
        mFeedbackButton.setTextColor(Color.WHITE);
        mFeedbackButton.setTextSize(14);
        mFeedbackButton.setBackgroundResource(CommonUtil.getResId(getContext(), "myoffer_bg_feedback_button", "drawable"));
        mFeedbackButton.setGravity(Gravity.CENTER);

        int paddingHorizontal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics());
        int paddingVertical = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getContext().getResources().getDisplayMetrics());
        mFeedbackButton.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 29, getContext().getResources().getDisplayMetrics());
        LayoutParams rl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, size);
        rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rl.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getContext().getResources().getDisplayMetrics());
        rl.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getContext().getResources().getDisplayMetrics());

        addView(mFeedbackButton, rl);

        mFeedbackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickFeedback();
                }
            }
        });
    }

    private void initLearnMoreButton() {
        mLearnMoreButton = new TextView(getContext());
        mLearnMoreButton.setText(CommonUtil.getResId(getContext(), "myoffer_cta_learn_more", "string"));
        mLearnMoreButton.setTextColor(Color.parseColor("#ffffffff"));
        mLearnMoreButton.setTextSize(20);
        mLearnMoreButton.setGravity(Gravity.CENTER);
        mLearnMoreButton.setBackgroundResource(CommonUtil.getResId(getContext(), "myoffer_splash_btn", "drawable"));

        int width = CommonUtil.dip2px(getContext(), 200);
        int height = CommonUtil.dip2px(getContext(), 70);
        int bottomMargin = CommonUtil.dip2px(getContext(), 23);

        LayoutParams layoutParams = new LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.bottomMargin = bottomMargin;

        mLearnMoreButton.setOnClickListener(mClickListener);

        addView(mLearnMoreButton, layoutParams);
    }


    private void attachTo(ViewGroup container) {
        if (container.getChildCount() == 2) {
            container.removeViewAt(0);
        }

        LayoutParams rl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.addView(this, 0, rl);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void removeFeedbackButton() {
        if (mFeedbackButton != null) {
            if (mFeedbackButton.getParent() != null) {
                ((ViewGroup) mFeedbackButton.getParent()).removeView(mFeedbackButton);
            }
        }
    }

    public interface OnEndCardListener {
        void onClick();

        void onCloseEndCard();

        void onClickFeedback();
    }
}
