package com.anythink.myoffer.ui;

import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.anythink.core.common.utils.CommonUtil;


public class LoadingView {

    private ViewGroup mRoot;
    private ImageView mLoadingIv;

    public LoadingView(ViewGroup container) {
        this.mRoot = container;

        addView();
        startLoading();
    }

    private void addView() {
        mLoadingIv = new ImageView(mRoot.getContext());
        mLoadingIv.setId(CommonUtil.getResId(mRoot.getContext(), "myoffer_loading_id", "id"));
        mLoadingIv.setImageResource(CommonUtil.getResId(mRoot.getContext(), "myoffer_loading", "drawable"));

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mRoot.getResources().getDisplayMetrics());
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(size, size);
        rl.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRoot.addView(mLoadingIv, rl);
    }

    private void startLoading() {
        mLoadingIv.post(new Runnable() {
            @Override
            public void run() {
                RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setDuration(1000);
                ra.setInterpolator(new LinearInterpolator());
                ra.setRepeatCount(-1);
                mLoadingIv.startAnimation(ra);
            }
        });
    }

    public void hide() {
        if(mLoadingIv != null) {
            mLoadingIv.clearAnimation();
            mRoot.removeView(mLoadingIv);
        }
    }

}
