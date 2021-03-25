/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.core.activity.component;



import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.anythink.core.common.base.Const;
import com.anythink.core.common.utils.CommonUtil;


public class LoadingView extends ImageView{

    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setImageDrawable(getResources().getDrawable(CommonUtil.getResId(getContext(), "core_loading", "drawable")));
        try{
            if (Build.VERSION.SDK_INT >= 19) {
                // chromium, enable hardware acceleration
                setLayerType(View.LAYER_TYPE_HARDWARE, null);
            } else {
                // older android version, disable hardware acceleration
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }catch (Exception ex){
            if(Const.DEBUG){
                ex.printStackTrace();
            }
        }
        startAnimation(this);

    }

    private void startAnimation(View mLoadingAnimation) {
        Animation mAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        mAnimation.setRepeatCount(-1);
        mAnimation.setInterpolator(lin);
        mAnimation.setDuration(1000);
        mLoadingAnimation.startAnimation(mAnimation);
    }

    public void startAnimation(){
        startAnimation(this);
    }

}
