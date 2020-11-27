/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.anythink.basead.ui.component.RoundImageView;
import com.anythink.basead.ui.util.ViewUtil;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.BitmapUtil;
import com.anythink.core.common.utils.CommonUtil;

public class EndCardView extends RelativeLayout {

    private OnEndCardListener mListener;
    private int mWidth;
    private int mHeight;

    private int mBlurBgIndex = 0;
    private int mEndCardIndex = 1;
    private int mCloseButtonIndex = 2;
    private ImageView mEndCardIv;
    private RoundImageView bgIv;

    public EndCardView(ViewGroup container, int width, int height, BaseAdContent myOfferAd, OnEndCardListener listener) {
        super(container.getContext());
        this.mListener = listener;

        this.mWidth = width;
        this.mHeight = height;

        init();
        attachTo(container);

        loadBitmap(myOfferAd);
    }

    private void loadBitmap(final BaseAdContent myOfferAd) {
        try {
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, myOfferAd.getEndCardImageUrl()), mWidth, mHeight, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(url, myOfferAd.getEndCardImageUrl())) {
                        mEndCardIv.setImageBitmap(bitmap);
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

    private void init() {
        bgIv = new RoundImageView(getContext());
        bgIv.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mEndCardIv = new RoundImageView(getContext());

        RelativeLayout.LayoutParams rl_bg = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        RelativeLayout.LayoutParams rl_endcard = new RelativeLayout.LayoutParams(mWidth, mHeight);
        rl_endcard.addRule(RelativeLayout.CENTER_IN_PARENT);

        addView(bgIv, mBlurBgIndex, rl_bg);
        addView(mEndCardIv, mEndCardIndex, rl_endcard);

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickEndCard();
                }
            }
        });

        initCloseButton();
    }

    private void initCloseButton() {

        if (getChildAt(mCloseButtonIndex) != null) {
            removeViewAt(mCloseButtonIndex);
        }

        ImageView mCloseBtn = new ImageView(getContext());
        mCloseBtn.setImageResource(CommonUtil.getResId(getContext(), "myoffer_video_close", "drawable"));

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 29, getContext().getResources().getDisplayMetrics());
        int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getContext().getResources().getDisplayMetrics());
        int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 19, getContext().getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(size, size);
        rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rl.rightMargin = rightMargin;
        rl.topMargin = topMargin;
        addView(mCloseBtn, mCloseButtonIndex, rl);

        //扩大点击区域
        ViewUtil.expandTouchArea(mCloseBtn, size / 2);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCloseEndCard();
                }
            }
        });
    }

    private void attachTo(ViewGroup container) {
        if (container.getChildCount() == 2) {
            container.removeViewAt(0);
        }

        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.addView(this, 0, rl);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public interface OnEndCardListener {
        void onClickEndCard();

        void onCloseEndCard();
    }
}
