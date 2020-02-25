package com.anythink.myoffer.ui;

import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.anythink.core.common.utils.CommonUtil;
import com.anythink.myoffer.buiness.resource.MyOfferImageUtil;
import com.anythink.myoffer.buiness.resource.MyOfferResourceUtil;
import com.anythink.myoffer.entity.MyOfferAd;
import com.anythink.myoffer.ui.util.ViewUtil;

public class EndCardView extends RelativeLayout {

    private OnEndCardListener mListener;
    private int mWidth;
    private int mHeight;

    private Bitmap mEndCardBitmap;
    private Bitmap mBlurBgBitmap;

    private int mBlurBgIndex = 0;
    private int mEndCardIndex = 1;
    private int mCloseButtonIndex = 2;
    private ImageView mEndCardIv;

    public EndCardView(ViewGroup container, int width, int height, MyOfferAd myOfferAd, OnEndCardListener listener) {
        super(container.getContext());
        this.mListener = listener;

        this.mWidth = width;
        this.mHeight = height;
        loadBitmap(myOfferAd);

        init();
        attachTo(container);
    }

    private void loadBitmap(MyOfferAd myOfferAd) {
        try {
            if(mEndCardBitmap == null) {
                String endCardImageUrl = myOfferAd.getEndCardImageUrl();
                mEndCardBitmap = MyOfferResourceUtil.getBitmap(endCardImageUrl, mWidth, mHeight);
            }
            if(mBlurBgBitmap == null && mEndCardBitmap != null) {
                mBlurBgBitmap = MyOfferImageUtil.blurBitmap(getContext(), mEndCardBitmap);
            }
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        ImageView bgIv = new ImageView(getContext());
        bgIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        bgIv.setImageBitmap(mBlurBgBitmap);

        mEndCardIv = new ImageView(getContext());
        mEndCardIv.setImageBitmap(mEndCardBitmap);

        RelativeLayout.LayoutParams rl_bg = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        RelativeLayout.LayoutParams rl_endcard = new RelativeLayout.LayoutParams(mWidth, mHeight);
        rl_endcard.addRule(RelativeLayout.CENTER_IN_PARENT);

        addView(bgIv, mBlurBgIndex, rl_bg);
        addView(mEndCardIv, mEndCardIndex, rl_endcard);

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onClickEndCard();
                }
            }
        });

        initCloseButton();
    }

    private void initCloseButton() {

        if(getChildAt(mCloseButtonIndex) != null) {
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
        rl.topMargin  = topMargin;
        addView(mCloseBtn, mCloseButtonIndex, rl);

        //扩大点击区域
        ViewUtil.expandTouchArea(mCloseBtn, size / 2);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onCloseEndCard();
                }
            }
        });
    }

    private void attachTo(ViewGroup container) {
        if(container.getChildCount() == 2) {
            container.removeViewAt(0);
        }

        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.addView(this, 0, rl);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mEndCardBitmap != null) {
            mEndCardBitmap.recycle();
        }
        if(mBlurBgBitmap != null) {
            mBlurBgBitmap.recycle();
        }
    }

    public interface OnEndCardListener {
        void onClickEndCard();
        void onCloseEndCard();
    }
}
