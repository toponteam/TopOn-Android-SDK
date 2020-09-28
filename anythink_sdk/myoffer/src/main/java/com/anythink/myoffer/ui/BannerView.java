package com.anythink.myoffer.ui;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anythink.core.common.entity.MyOfferAd;
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

    public BannerView(ViewGroup container, MyOfferAd myOfferAd, OnBannerListener listener) {
        super(container.getContext());
        this.mListener = listener;

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

    private void setDataFrom(MyOfferAd myOfferAd) {

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

        mTvTitle.setText(myOfferAd.getTitle());
        mTvDesc.setText(myOfferAd.getDesc());
        mBtnCTA.setText(myOfferAd.getCtaText());
    }

    private void attachTo(ViewGroup container) {
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getContext().getResources().getDisplayMetrics());

        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
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
