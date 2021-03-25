/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.basead.ui.util.ViewUtil;
import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.BaseAdSetting;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.CommonUtil;

public class BannerView extends RelativeLayout {

    private View mView;
    private ImageView mIvIcon;
    private ImageView mIvLogo;
    private TextView mTvTitle;
    private TextView mTvDesc;
    private Button mBtnCTA;

    private OnBannerListener mListener;

    private int mOrientation;
    private BaseAdSetting mBaseAdSetting;

    public BannerView(ViewGroup container, BaseAdContent baseAdContent, BaseAdSetting baseAdSetting, int orientation, OnBannerListener listener) {
        super(container.getContext());
        this.mListener = listener;
        mOrientation = orientation;
        mBaseAdSetting = baseAdSetting;

        initView();
        setDataFrom(baseAdContent);
        setListener();
        attachTo(container);
    }

    private void initView() {
        mView = LayoutInflater.from(getContext()).inflate(
                CommonUtil.getResId(getContext(), "myoffer_bottom_banner", "layout")
                , this, true);
        setId(CommonUtil.getResId(getContext(), "myoffer_banner_view_id", "id"));

        mIvIcon = mView.findViewById(CommonUtil.getResId(getContext(), "myoffer_iv_banner_icon", "id"));
        mTvTitle = mView.findViewById(CommonUtil.getResId(getContext(), "myoffer_tv_banner_title", "id"));
        mTvDesc = mView.findViewById(CommonUtil.getResId(getContext(), "myoffer_tv_banner_desc", "id"));
        mBtnCTA = mView.findViewById(CommonUtil.getResId(getContext(), "myoffer_btn_banner_cta", "id"));
        mIvLogo = mView.findViewById(CommonUtil.getResId(getContext(), "myoffer_iv_logo", "id"));
    }

    private void setDataFrom(BaseAdContent baseAdContent) {

        ViewGroup.LayoutParams lp;
        int width;
        int height;
        final String iconUrl = baseAdContent.getIconUrl();//icon
        if (!TextUtils.isEmpty(iconUrl)) {
            lp = mIvIcon.getLayoutParams();
            width = lp.width;
            height = lp.height;
            ImageLoader.getInstance(getContext()).load(new ResourceEntry(ResourceEntry.INTERNAL_CACHE_TYPE, iconUrl), width, height, new ImageLoader.ImageLoaderListener() {
                @Override
                public void onSuccess(String url, Bitmap bitmap) {
                    if (TextUtils.equals(url, iconUrl)) {
                        mIvIcon.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onFail(String url, String errorMsg) {

                }
            });

        }

        final String logoUrl = baseAdContent.getAdChoiceUrl();//logo
        if (!TextUtils.isEmpty(logoUrl)) {
            lp = mIvLogo.getLayoutParams();
            width = lp.width;
            height = lp.height;
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

        if (TextUtils.isEmpty(baseAdContent.getIconUrl())) {
            mIvIcon.setVisibility(GONE);
        }
//
        if (TextUtils.isEmpty(baseAdContent.getDesc())) {
            mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            mTvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            mTvDesc.setVisibility(GONE);
        }

        mTvTitle.setText(baseAdContent.getTitle());
        mTvDesc.setText(baseAdContent.getDesc());

        if (!TextUtils.isEmpty(baseAdContent.getCtaText())) {
            mBtnCTA.setVisibility(View.VISIBLE);
            mBtnCTA.setText(baseAdContent.getCtaText());
        } else {
            mBtnCTA.setVisibility(View.GONE);
        }
    }

    private void attachTo(ViewGroup container) {
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());
        int width = LayoutParams.MATCH_PARENT;

        int height = CommonUtil.dip2px(getContext(), 73);
        if (mIvIcon.getVisibility() != VISIBLE) {
            height = CommonUtil.dip2px(getContext(), 60);
        }

//        if (mTvDesc.getVisibility() != VISIBLE) {
//            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
//                width = getContext().getResources().getDisplayMetrics().widthPixels * 3 / 5;
//            }
//        }

        LayoutParams rl = new LayoutParams(width, height);
        rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rl.leftMargin = margin;
        rl.rightMargin = margin;
        rl.bottomMargin = margin;
        container.addView(this, rl);
    }

    private void setListener() {
        mIvIcon.setOnClickListener(mClickListener);
        mTvTitle.setOnClickListener(mClickListener);
        mTvDesc.setOnClickListener(mClickListener);
        mBtnCTA.setOnClickListener(mClickListener);
        mIvLogo.setOnClickListener(mClickListener);

        mView.setOnClickListener(mClickListener);
    }

    private final OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBaseAdSetting != null) {
                if (mBaseAdSetting.getEndCardClickArea() == 1) {// cta
                     if (v == mBtnCTA) {
                         if (mListener != null) {
                             mListener.onClick();
                         }
                     }
                 } else {//fullscreen or banner area
                     if (mListener != null) {
                         mListener.onClick();
                     }
                 }
            }
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public interface OnBannerListener {
        void onClick();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        ViewUtil.drawRadiusMask(canvas, getWidth(), getHeight(), CommonUtil.dip2px(getContext(), 7));
        canvas.restoreToCount(saveCount);
    }
}
