package com.anythink.myoffer.ui;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.anythink.core.common.utils.CommonUtil;
import com.anythink.myoffer.buiness.resource.MyOfferResourceUtil;
import com.anythink.myoffer.entity.MyOfferAd;

public class MainImageView extends RelativeLayout {

    private ViewGroup mContainer;
    private OnMainImgListener mListener;
    private int mWidth;
    private int mHeight;

    private Bitmap mMainImageBitmap;

    private int mMainImageIndex = 0;
    private int mCloseButtonIndex = 1;


    public MainImageView(ViewGroup container, int width, int height, MyOfferAd myOfferAd, MainImageView.OnMainImgListener listener) {
        super(container.getContext());
        this.mContainer = container;
        this.mListener = listener;

        this.mWidth = width;
        this.mHeight = height;
        loadBitmap(myOfferAd);

        init();
        attachTo(container);
    }

    private void loadBitmap(MyOfferAd myOfferAd) {
        try {
            if(mMainImageBitmap == null) {
                String mainImageUrl = myOfferAd.getMainImageUrl();
                if(!TextUtils.isEmpty(mainImageUrl)) {
                    mMainImageBitmap = MyOfferResourceUtil.getBitmap(mainImageUrl, mWidth, mHeight);
                }
            }
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {

        ImageView imageView = new ImageView(getContext());
        imageView.setId(CommonUtil.getResId(getContext(), "myoffer_main_image_id", "id"));
        if(mMainImageBitmap != null) {
            imageView.setImageBitmap(mMainImageBitmap);
        }

        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(mWidth, mHeight);
        rl.addRule(RelativeLayout.CENTER_IN_PARENT);

        addView(imageView, mMainImageIndex, rl);

        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onClickMainImage();
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
        mCloseBtn.setId(CommonUtil.getResId(getContext(), "myoffer_btn_close_id", "id"));
        mCloseBtn.setImageResource(CommonUtil.getResId(getContext(), "myoffer_video_close", "drawable"));

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 29, getContext().getResources().getDisplayMetrics());
        int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getContext().getResources().getDisplayMetrics());
        int rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 19, getContext().getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(size, size);
        rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rl.rightMargin = rightMargin;
        rl.topMargin  = topMargin;
        addView(mCloseBtn, mCloseButtonIndex, rl);

        mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.onCloseMainImage();
                }
            }
        });
    }

    private void attachTo(ViewGroup container) {
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        container.addView(this, 0, rl);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mMainImageBitmap != null) {
            mMainImageBitmap.recycle();
        }
    }

    public interface OnMainImgListener {
        void onClickMainImage();
        void onCloseMainImage();
    }
}
