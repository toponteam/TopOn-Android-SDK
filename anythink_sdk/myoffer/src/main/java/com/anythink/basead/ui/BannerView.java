/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.content.res.Configuration;
import android.graphics.Bitmap;
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

import com.anythink.core.common.entity.BaseAdContent;
import com.anythink.core.common.entity.MyOfferAd;
import com.anythink.core.common.res.ImageLoader;
import com.anythink.core.common.res.ResourceEntry;
import com.anythink.core.common.utils.CommonUtil;

import java.lang.reflect.Type;

public class BannerView extends RelativeLayout {

    private View mView;
    private ImageView mIvIcon;
    private ImageView mIvLogo;
    private TextView mTvTitle;
    private TextView mTvDesc;
    private Button mBtnCTA;

    private OnBannerListener mListener;

    private int mOrientation;

    public BannerView(ViewGroup container, BaseAdContent myOfferAd, int orientation, OnBannerListener listener) {
        super(container.getContext());
        this.mListener = listener;
        mOrientation = orientation;

        initView();
        setDataFrom(myOfferAd);
        setListener();
        //添加布局
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

    private void setDataFrom(BaseAdContent myOfferAd) {

        ViewGroup.LayoutParams lp;
        int width;
        int height;
        final String iconUrl = myOfferAd.getIconUrl();//icon
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

        final String logoUrl = myOfferAd.getAdChoiceUrl();//logo
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

        if (TextUtils.isEmpty(myOfferAd.getIconUrl())) {
            mIvIcon.setVisibility(GONE);
        }
//
        if (TextUtils.isEmpty(myOfferAd.getDesc())) {
            mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            mTvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            mTvDesc.setVisibility(GONE);
        }

        mTvTitle.setText(myOfferAd.getTitle());
        mTvDesc.setText(myOfferAd.getDesc());
        mBtnCTA.setText(myOfferAd.getCtaText());
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

        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(width, height);
        rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rl.leftMargin = margin;
        rl.rightMargin = margin;
        rl.bottomMargin = margin;
        container.addView(this, rl);
    }

    private void setListener() {
        mBtnCTA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickCTA();
                }
            }
        });
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClickBanner();
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public interface OnBannerListener {
        void onClickCTA();

        void onClickBanner();
    }

}
