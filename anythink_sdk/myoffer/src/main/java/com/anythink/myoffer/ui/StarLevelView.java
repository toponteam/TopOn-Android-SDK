package com.anythink.myoffer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.anythink.core.common.utils.CommonUtil;

public class StarLevelView extends ImageView {
    Context mContext;

    public StarLevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public StarLevelView(Context context) {
        this(context, null);
        mContext = context;
    }

    public StarLevelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void setState(boolean state) {
        if (state) {
            setImageResource(CommonUtil.getResId(getContext(),"myoffer_splash_star","drawable"));
        } else {
            setImageResource(CommonUtil.getResId(getContext(),"myoffer_splash_star_gray","drawable"));
        }
    }

}
