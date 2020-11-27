/*
 * Copyright © 2018-2020 TopOn. All rights reserved.
 * https://www.toponad.com
 * Licensed under the TopOn SDK License Agreement
 * https://github.com/toponteam/TopOn-Android-SDK/blob/master/LICENSE
 */

package com.anythink.basead.ui.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class RoundFrameLayout extends FrameLayout {

    int mRadiu;

    public RoundFrameLayout(Context context) {
        super(context);
        mRadiu = dip2px(getContext(), 10);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRadiu = dip2px(getContext(), 10);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRadiu = dip2px(getContext(), 10);
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            int width = getWidth();
            int height = getHeight();

            Path path = new Path();
            path.moveTo(mRadiu, 0);

            path.lineTo(width - mRadiu, 0);
            path.quadTo(width, 0, width, mRadiu);

            path.lineTo(width, height - mRadiu);
            path.quadTo(width, height, width - mRadiu, height);

            path.lineTo(mRadiu, height);
            path.quadTo(0, height, 0, height - mRadiu);

            path.lineTo(0, mRadiu);
            path.quadTo(0, 0, mRadiu, 0);
            canvas.clipPath(path);
            super.dispatchDraw(canvas);
        } catch (Exception e) {

        }
    }

    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
