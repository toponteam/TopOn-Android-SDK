/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.anythink.core.common.utils.CommonUtil;

public class AdTextView extends TextView {

    public AdTextView(Context context) {
        super(context);
        init(context);
    }

    public AdTextView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AdTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        setBackgroundResource(CommonUtil.getResId(context, "myoffer_bg_banner_ad_choice", "drawable"));
        setTextColor(Color.WHITE);
        setText("AD");
        setTextSize(8);
        setGravity(Gravity.CENTER);
        setPadding(CommonUtil.dip2px(context, 3), 0, CommonUtil.dip2px(context, 3), 0);
    }
}
