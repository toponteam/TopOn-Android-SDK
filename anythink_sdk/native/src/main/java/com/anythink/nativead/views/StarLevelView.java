/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.nativead.views;

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
            setImageResource(CommonUtil.getResId(getContext(),"plugin_splash_star","drawable"));
        } else {
            setImageResource(CommonUtil.getResId(getContext(),"plugin_splash_star_gray","drawable"));
        }
    }

}
